import React, {useEffect, useMemo, useState} from "react";
import {
  Alert,
  Badge,
  Button,
  Card,
  Col,
  Form,
  InputGroup,
  ListGroup,
  Row,
  Accordion
} from "react-bootstrap";
import QRCode from "react-qr-code";

/**
 * Credential Offer Editor (OID4VCI)
 *
 * Props:
 * - oid4vciMetadata: object
 * - oidcMetadata: object (optional)
 * - credentialIssuer: string
 *
 * Behavior:
 * - Draft state changes freely
 * - QR-Code & Deep-Link are regenerated ONLY via button click / enter-submit
 */
export function CredentialOfferEditor({oid4vciMetadata, oidcMetadata, credentialIssuer})
{
  /* ------------------------------------------------------------------
   * Metadata helpers
   * ------------------------------------------------------------------ */

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

  const defaultAuthorizationServer = useMemo(() => {
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
  const [selectedFromDropdown, setSelectedFromDropdown] = useState([]);
  const [manualIdInput, setManualIdInput] = useState("");
  const [manualIds, setManualIds] = useState([]);

  /* ------------------------------------------------------------------
   * Committed output (QR + Deep-Link)
   * ------------------------------------------------------------------ */

  const [committedOffer, setCommittedOffer] = useState(null);
  const [committedDeepLink, setCommittedDeepLink] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    setAuthorizationServerDraft(prev => prev || defaultAuthorizationServer);
  }, [defaultAuthorizationServer]);

  const mergedIds = useMemo(() => {
    const merged = [...selectedFromDropdown, ...manualIds]
      .map(v => (v || "").trim())
      .filter(Boolean);

    return Array.from(new Set(merged));
  }, [selectedFromDropdown, manualIds]);

  const issuer = String(credentialIssuer || "").trim();
  const authServerDraftTrimmed = String(authorizationServerDraft || "").trim();

  const canGenerate =
    Boolean(issuer) &&
    Boolean(authServerDraftTrimmed) &&
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

  function onDropdownChange(e)
  {
    const values = Array.from(e.target.selectedOptions).map(o => o.value);
    setSelectedFromDropdown(values);
  }

  function generateOffer()
  {
    setError("");

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

    if (!authServerDraftTrimmed)
    {
      setError("authorization_server is empty.");
      return;
    }

    // Spec: authorization_server is a parameter under grants.authorization_code
    // and MUST NOT be used unless issuer metadata has multiple authorization_servers entries.
    const shouldIncludeAuthorizationServer = authorizationServers.length > 1;

    if (shouldIncludeAuthorizationServer)
    {
      const matches = authorizationServers.includes(authServerDraftTrimmed);
      if (!matches)
      {
        setError(
          "authorization_server must match one of the authorization_servers from metadata when multiple are present."
        );
        return;
      }
    }

    const offer = {
      credential_issuer: issuer,
      credential_configuration_ids: mergedIds,
      // grants is optional in spec; we include it to explicitly request authorization_code
      grants: {
        authorization_code: {
          ...(shouldIncludeAuthorizationServer ? {authorization_server: authServerDraftTrimmed} : {})
          // issuer_state could be added here in the future, if you want it configurable
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
      <Row className="g-3">

        {/* ============================================================
         LEFT: INPUT EDITOR
         ============================================================ */}
        <Col xl={7} lg={7} md={12}>
          <Card>
            <Card.Header className="d-flex justify-content-between align-items-center">
              <div>
                <strong>Credential Offer</strong>{" "}
                <Badge bg="secondary">authorization_code</Badge>
              </div>

              {/* type=submit so Enter triggers generate */}
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

              {/* Enter anywhere in this form triggers submit -> generateOffer */}
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
                  <Form.Control
                    readOnly
                    value={credentialIssuer || ""}
                  />
                </Form.Group>

                {/* Authorization Server */}
                <Form.Group className="mb-3">
                  <Form.Label>Authorization Server</Form.Label>
                  <Form.Control
                    value={authorizationServerDraft}
                    onChange={e => setAuthorizationServerDraft(e.target.value)}
                  />
                  <Form.Text muted>
                    {authorizationServers.length > 1
                      ? "Multiple authorization servers detected in metadata. The value must match one of them."
                      : "Only one authorization server in metadata. The authorization_server parameter will not be included in the offer (per spec)."}
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
                            // Enter here should ADD, not submit
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

        {/* ============================================================
         RIGHT: OUTPUT
         ============================================================ */}
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
                  {/* Deep Link */}
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
                          try { await navigator.clipboard.writeText(committedDeepLink); }
                          catch (e) {}
                        }}
                      >
                        Copy
                      </Button>
                    </InputGroup>

                    {/* Accordion for full URL */}
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

                  {/* QR */}
                  <div className="qr-container mb-3">
                    <QRCode
                      value={committedDeepLink}
                      style={{width: "100%", maxWidth: 260, height: "auto"}}
                    />
                  </div>

                  {/* JSON (scrollable) */}
                  <Form.Group>
                    <Form.Label>Credential Offer JSON</Form.Label>
                    <Form.Control
                      as="textarea"
                      readOnly
                      value={JSON.stringify(committedOffer, null, 2)}
                      className="credential-offer-json"
                      style={{
                        fontFamily: "monospace",
                        height: "260px",      // fixed height so scrolling works
                        overflowY: "auto",
                        resize: "vertical"    // user can resize if desired
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
