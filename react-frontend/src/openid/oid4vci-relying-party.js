import React, {useEffect, useMemo, useRef, useState} from "react";
import {Accordion, Alert, Badge, Button, Card, Col, Form, InputGroup, ListGroup, Row, Spinner} from "react-bootstrap";
import QRCode from "react-qr-code";
import {ScimClient2} from "../scim/scim-client-2";
import {useParams} from "react-router-dom";
import {ArrowRightCircle} from "react-bootstrap-icons";
import {LinkContainer} from "react-router-bootstrap";
import {AiOutlineWarning} from "react-icons/ai";
import {Oid4vciMetadataViewer} from "./oid4vci-metadata-viewer";

export function CredentialOfferEditor()
{
  const params = useParams();
  const [oid4vciMetadata, setOid4vciMetadata] = useState();
  const [oidcMetadata, setOidcMetadata] = useState();
  const [loading, setLoading] = useState(true);

  let openIdProviderId = params.providerId;

  // NOTE: you fixed this already in your local version – keep it like that:
  // let credentialIssuer = oid4vciMetadata?.credent...
  // In this file we keep your intent: issuer comes from OID4VCI metadata
  let credentialIssuer = oid4vciMetadata?.credential_issuer;

  /* ------------------------------------------------------------------
   * Metadata helpers
   * ------------------------------------------------------------------ */

  const [error, setError] = useState("");

  useEffect(() => {
    let metadataResourcePath = "/scim/v2/ProviderMetadata";
    const safeParse = (val) => {
      if (!val)
      {
        return null;
      }
      if (typeof val === "object")
      {
        return val;
      }
      if (typeof val !== "string")
      {
        return null;
      }
      try
      {
        return JSON.parse(val);
      }
      catch (e)
      {
        return null;
      }
    };

    let onSuccess = metadata => {
      setLoading(false)
      const oidc = safeParse(metadata.oidcMetadata);
      const oid4vci = safeParse(metadata.oid4vciMetadata);
      setOidcMetadata(oidc);
      setOid4vciMetadata(oid4vci);
    };

    let onError = errorResponse => {
      setLoading(false)
      setError(errorResponse.detail);
    }

    new ScimClient2().getResource(metadataResourcePath, openIdProviderId, null, onSuccess, onError);
  }, []);

  const authorizationServers = useMemo(() => {
    const servers = oid4vciMetadata?.authorization_servers;
    return Array.isArray(servers) ? servers : [];
  }, [oid4vciMetadata]);

  const availableCredentialConfigIds = useMemo(() => {
    if (!oid4vciMetadata)
    {
      return [];
    }

    const cfgs =
      oid4vciMetadata.credential_configurations_supported ||
      oid4vciMetadata.credential_configurations ||
      null;

    if (cfgs && typeof cfgs === "object" && !Array.isArray(cfgs))
    {
      return Object.keys(cfgs);
    }

    if (Array.isArray(oid4vciMetadata.credential_configuration_ids_supported))
    {
      return oid4vciMetadata.credential_configuration_ids_supported;
    }

    return [];
  }, [oid4vciMetadata]);

  // kept (might be useful later), but selection defaults are now driven by authorizationServers length
  useMemo(() => {
    if (authorizationServers.length > 0)
    {
      return authorizationServers[0];
    }

    if (oidcMetadata?.issuer)
    {
      return oidcMetadata.issuer;
    }

    return "";
  }, [authorizationServers, oidcMetadata]);

  /* ------------------------------------------------------------------
   * Draft state
   * ------------------------------------------------------------------ */

  const [authorizationServerDraft, setAuthorizationServerDraft] = useState("");
  const [customAuthorizationServerInput, setCustomAuthorizationServerInput] = useState("");
  const [customAuthorizationServers, setCustomAuthorizationServers] = useState([]);

  const [selectedFromDropdown, setSelectedFromDropdown] = useState([]);
  const [manualIdInput, setManualIdInput] = useState("");
  const [manualIds, setManualIds] = useState([]);

  /* ------------------------------------------------------------------
   * Committed output (QR + Deep-Link)
   * ------------------------------------------------------------------ */

  const [committedOffer, setCommittedOffer] = useState(null);
  const [committedDeepLink, setCommittedDeepLink] = useState("");
  const [warning, setWarning] = useState("");
  const didAutoInitRef = useRef(false);
  const pendingAutoGenerateRef = useRef(false);

  useEffect(() => {
    // Default selection rules:
    // - If exactly one authorization server is advertised: default to EMPTY (user may optionally select it)
    // - If multiple are advertised: preselect the first
    // - If none are advertised: keep empty
    setAuthorizationServerDraft(prev => {
      if (prev && String(prev).trim().length > 0)
      {
        return prev;
      }
      if (authorizationServers.length > 1)
      {
        return authorizationServers[0] || "";
      }
      return "";
    });
  }, [authorizationServers]);

  const mergedIds = useMemo(() => {
    const merged = [...selectedFromDropdown, ...manualIds]
      .map(v => (v || "").trim())
      .filter(Boolean);

    return Array.from(new Set(merged));
  }, [selectedFromDropdown, manualIds]);

  const authorizationServerCandidates = useMemo(() => {
    const merged = [...authorizationServers, ...customAuthorizationServers]
      .map(v => (v || "").trim())
      .filter(Boolean);
    return Array.from(new Set(merged));
  }, [authorizationServers, customAuthorizationServers]);

  const issuer = String(credentialIssuer || "").trim();
  const authServerDraftTrimmed = String(authorizationServerDraft || "").trim();

  // Auto-select first credential_configuration_id after metadata is loaded the first time,
  // then generate the QR code once.
  useEffect(() => {
    if (didAutoInitRef.current)
    {
      return;
    }
    // Only run once, after we actually have metadata-derived IDs.
    if (!oid4vciMetadata || availableCredentialConfigIds.length === 0)
    {
      return;
    }

    didAutoInitRef.current = true;

    // Do not override user input if something is already selected/entered.
    const hasAnyIdsSelected = (selectedFromDropdown && selectedFromDropdown.length > 0) ||
      (manualIds && manualIds.length > 0);

    if (!hasAnyIdsSelected)
    {
      setSelectedFromDropdown([availableCredentialConfigIds[0]]);
      pendingAutoGenerateRef.current = true;
    }
  }, [oid4vciMetadata, availableCredentialConfigIds, selectedFromDropdown, manualIds]);

  useEffect(() => {
    if (!pendingAutoGenerateRef.current)
    {
      return;
    }
    if (!issuer || mergedIds.length === 0)
    {
      return;
    }

    pendingAutoGenerateRef.current = false;
    generateOffer();
  }, [issuer, mergedIds]);

  const canGenerate =
    Boolean(issuer) &&
    mergedIds.length > 0;

  /* ------------------------------------------------------------------
   * Actions
   * ------------------------------------------------------------------ */

  function addManualId()
  {
    const id = (manualIdInput || "").trim();
    if (!id)
    {
      return;
    }

    setManualIds(prev => (prev.includes(id) ? prev : [...prev, id]));
    setManualIdInput("");
  }

  function removeManualId(id)
  {
    setManualIds(prev => prev.filter(x => x !== id));
  }

  function addCustomAuthorizationServer()
  {
    const value = (customAuthorizationServerInput || "").trim();
    if (!value)
    {
      return;
    }

    setCustomAuthorizationServers(prev => (prev.includes(value) ? prev : [...prev, value]));
    setAuthorizationServerDraft(value);
    setCustomAuthorizationServerInput("");
  }

  function clearAuthorizationServerSelection()
  {
    setAuthorizationServerDraft("");
  }

  function onDropdownChange(e)
  {
    const values = Array.from(e.target.selectedOptions).map(o => o.value);
    setSelectedFromDropdown(values);
  }

  function generateOffer()
  {
    setError("");
    setWarning("");

    if (!issuer)
    {
      setError("Missing credential issuer (credential_issuer).");
      return;
    }

    if (!mergedIds.length)
    {
      setError("Please select at least one credential_configuration_id.");
      return;
    }

    // authorization_server is a parameter under grants.authorization_code.
    // We include it whenever the user selected/entered a value.
    const shouldIncludeAuthorizationServer = Boolean(authServerDraftTrimmed);

    if (shouldIncludeAuthorizationServer && authorizationServers.length > 0 && authorizationServers.length > 1)
    {
      const matches = authorizationServers.includes(authServerDraftTrimmed);
      if (!matches)
      {
        // Keep it usable for interop/testing, but make it very visible.
        setWarning(
          "Spec note: When multiple authorization servers are advertised in metadata, authorization_server should match one of those values. (Kept for interop/testing.)"
        );
      }
    }

    const offer = {
      credential_issuer: issuer,
      credential_configuration_ids: mergedIds,
      grants: {
        authorization_code: {
          ...(shouldIncludeAuthorizationServer ? {authorization_server: authServerDraftTrimmed} : {})
        }
      }
    };

    const deepLink =
      "openid-credential-offer://?credential_offer=" +
      encodeURIComponent(JSON.stringify(offer));

    setCommittedOffer(offer);
    setCommittedDeepLink(deepLink);
  }

  /* ------------------------------------------------------------------
   * Render
   * ------------------------------------------------------------------ */

  return (
    <div className="oidc-credential-offer-editor">

      <div className="d-flex justify-content-between align-items-center mb-3">
        <LinkContainer to={"/views/openIdProvider/"}>
          <a href={"/#"} className={"action-link me-5"}>
            <h5 className={"mobile-aware-title"}>
              <ArrowRightCircle style={{marginRight: "15px"}} size={"20px"} height={"30px"} />
              Back to OpenID Providers
            </h5>
          </a>
        </LinkContainer>
      </div>

      <Row className="g-3 oidc-credential-offer-editor">
        <Accordion className={"deep-link-accordion"}>
          <Accordion.Item eventKey="0">
            <Accordion.Header>OID4VCI Metadata</Accordion.Header>
            <Accordion.Body>
              <Oid4vciMetadataViewer oid4vciMetadata={oid4vciMetadata} />
            </Accordion.Body>
          </Accordion.Item>
        </Accordion>
      </Row>

      <Row className="g-3">

        {/* LEFT */}
        <Col xl={7} lg={7} md={12}>
          <Card>
            <Card.Header className="d-flex justify-content-between align-items-center">
              <div>
                <strong>Credential Offer</strong>{" "}
                <Badge bg="secondary">authorization_code</Badge>
              </div>

              <Button
                variant="warning"
                type="submit"
                form="credential-offer-form"
                disabled={!canGenerate}
              >
                Update QR
              </Button>
            </Card.Header>

            <Card.Body>
              {error && (
                <Alert variant="danger">
                  {error}
                </Alert>
              )}
              {loading && (
                <Alert variant="warning">
                  <Spinner variant={"warning"} /> Loading...
                </Alert>
              )}

              {warning && (
                <Alert variant="warning" className="d-flex align-items-center gap-2">
                  <AiOutlineWarning />
                  <div>{warning}</div>
                </Alert>
              )}

              <Form
                id="credential-offer-form"
                onSubmit={(e) => {
                  e.preventDefault();
                  if (canGenerate)
                  {
                    generateOffer();
                  }
                }}
              >
                {/* Issuer */}
                <Form.Group className="mb-3">
                  <Form.Label>Credential Issuer</Form.Label>
                  <Form.Control value={credentialIssuer || ""}
                  />
                </Form.Group>

                {/* Authorization Server */}
                <Form.Group className="mb-3">
                  <Form.Label>Authorization Server</Form.Label>

                  {authorizationServerCandidates.length > 0 && (
                    <Form.Select
                      className="mb-2"
                      value={authorizationServerDraft}
                      onChange={e => setAuthorizationServerDraft(e.target.value)}
                    >
                      <option value="">(select authorization server)</option>
                      {authorizationServerCandidates.map(s => (
                        <option key={s} value={s}>{s}</option>
                      ))}
                    </Form.Select>
                  )}

                  {authorizationServerDraft && (
                    <div className="d-flex align-items-center gap-2 mb-2">
                      <Badge bg="info">Selected</Badge>
                      <code className="text-break">{authorizationServerDraft}</code>
                      <Button
                        size="sm"
                        variant="outline-light"
                        onClick={clearAuthorizationServerSelection}
                      >
                        Clear
                      </Button>
                    </div>
                  )}

                  <InputGroup>
                    <Form.Control
                      placeholder="Add custom authorization server URL"
                      value={customAuthorizationServerInput}
                      onChange={e => setCustomAuthorizationServerInput(e.target.value)}
                      onKeyDown={e => {
                        if (e.key === "Enter")
                        {
                          e.preventDefault();
                          addCustomAuthorizationServer();
                        }
                      }}
                    />
                    <Button variant="outline-light" onClick={addCustomAuthorizationServer}>Add</Button>
                  </InputGroup>

                  <Form.Text muted>
                    {authorizationServers.length > 1
                      ? "Multiple authorization servers detected in metadata. If you select one, it will be included in the offer."
                      : "Only one authorization server in metadata. Selection is optional; if selected it will be included in the offer."}
                  </Form.Text>
                </Form.Group>

                <Row className="g-3">
                  {/* Dropdown */}
                  <Col md={6}>
                    <Form.Group>
                      <Form.Label>credential_configuration_ids</Form.Label>
                      <Form.Select
                        multiple
                        value={selectedFromDropdown}
                        onChange={onDropdownChange}
                        style={{minHeight: 180}}
                      >
                        {availableCredentialConfigIds.map(id => (
                          <option key={id} value={id}>{id}</option>
                        ))}
                      </Form.Select>
                    </Form.Group>
                  </Col>

                  {/* Manual Add */}
                  <Col md={6}>
                    <Form.Group>
                      <Form.Label>Add ID manually</Form.Label>
                      <InputGroup>
                        <Form.Control
                          value={manualIdInput}
                          onChange={e => setManualIdInput(e.target.value)}
                          onKeyDown={e => {
                            if (e.key === "Enter")
                            {
                              e.preventDefault();
                              addManualId();
                            }
                          }}
                        />
                        <Button variant="outline-light" onClick={addManualId}>
                          Add
                        </Button>
                      </InputGroup>

                      {manualIds.length > 0 && (
                        <ListGroup className="mt-2">
                          {manualIds.map(id => (
                            <ListGroup.Item
                              key={id}
                              className="d-flex justify-content-between align-items-center"
                            >
                              <code>{id}</code>
                              <Button
                                size="sm"
                                variant="outline-danger"
                                onClick={() => removeManualId(id)}
                              >
                                Remove
                              </Button>
                            </ListGroup.Item>
                          ))}
                        </ListGroup>
                      )}
                    </Form.Group>
                  </Col>
                </Row>

                {/* Selected preview */}
                <Form.Group className="mt-3">
                  <Form.Label>Selected IDs</Form.Label>
                  <div className="d-flex flex-wrap gap-2">
                    {mergedIds.map(id => (
                      <Badge key={id} bg="info">{id}</Badge>
                    ))}
                  </div>
                </Form.Group>
              </Form>
            </Card.Body>
          </Card>
        </Col>

        {/* RIGHT */}
        <Col xl={5} lg={5} md={12}>
          <Card className="credential-offer-output">
            <Card.Header>
              <strong>Deep Link &amp; QR</strong>
            </Card.Header>

            <Card.Body>
              {!committedDeepLink ? (
                <Alert variant="secondary">
                  Configure the offer and click <strong>Update QR</strong> (or press Enter).
                </Alert>
              ) : (
                <>
                  <Form.Group className="mb-3">
                    <Form.Label>Deep Link (same-device)</Form.Label>

                    <InputGroup className="mb-2">
                      <Button
                        as="a"
                        href={committedDeepLink}
                        variant="primary"
                      >
                        Open in Wallet
                      </Button>

                      <Form.Control
                        className="deep-link-field"
                        readOnly
                        value={committedDeepLink}
                        onFocus={e => e.target.select()}
                      />

                      <Button
                        variant="outline-light"
                        onClick={async () => {
                          try
                          {
                            await navigator.clipboard.writeText(committedDeepLink);
                          }
                          catch (e)
                          {
                          }
                        }}
                      >
                        Copy
                      </Button>
                    </InputGroup>

                    <Accordion className="deep-link-accordion">
                      <Accordion.Item eventKey="0">
                        <Accordion.Header>
                          Show full deep link
                        </Accordion.Header>
                        <Accordion.Body>
                          <div className="deep-link-full">
                            {committedDeepLink}
                          </div>
                        </Accordion.Body>
                      </Accordion.Item>
                    </Accordion>
                  </Form.Group>

                  <div className="qr-container mb-3">
                    <QRCode
                      value={committedDeepLink}
                      style={{width: "100%", maxWidth: 260, height: "auto"}}
                    />
                  </div>

                  <Form.Group>
                    <Form.Label>Credential Offer JSON</Form.Label>
                    <Form.Control
                      as="textarea"
                      readOnly
                      value={JSON.stringify(committedOffer, null, 2)}
                      className="credential-offer-json"
                      style={{
                        fontFamily: "monospace",
                        height: "260px",
                        overflowY: "auto",
                        resize: "vertical"
                      }}
                    />
                  </Form.Group>
                </>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
