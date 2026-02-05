import React, {useMemo, useState} from "react";
import {
  Accordion,
  Badge,
  Button,
  ButtonGroup,
  Card,
  Col,
  Form,
  InputGroup,
  ListGroup,
  Row,
  Table,
} from "react-bootstrap";
import {AiOutlineCopy, AiOutlineDownload, AiOutlineSafetyCertificate, AiOutlineSearch} from "react-icons/ai";
import {X5cPathHeader} from "../services/certificate-download";

/**
 * OID4VCI Metadata Viewer (read-only)
 * - No new CSS
 * - Uses existing CredentialOfferEditor styles (deep-link-accordion, oidc-credential-offer-editor)
 * - Adds x5c download UI for any x5c chains found in metadata
 */
export function Oid4vciMetadataViewer({oid4vciMetadata, title = "OID4VCI Metadata"})
{
  const [filter, setFilter] = useState("");
  const [expandedIds, setExpandedIds] = useState(new Set());

  const meta = oid4vciMetadata || null;

  const issuer = meta?.credential_issuer || meta?.issuer || "";
  const authServers = Array.isArray(meta?.authorization_servers) ? meta.authorization_servers : [];
  const endpoints = useMemo(() => extractEndpoints(meta), [meta]);

  const x5cChains = useMemo(() => extractX5cChains(meta), [meta]);

  const credentialConfigs = useMemo(() => {
    const cfgs = meta?.credential_configurations_supported;
    if (!cfgs || typeof cfgs !== "object" || Array.isArray(cfgs))
    {
      return [];
    }
    return Object.entries(cfgs).map(([id, cfg]) => ({
      id,
      cfg: cfg ?? {},
      format: cfg?.format,
    }));
  }, [meta]);

  const filteredCredentialConfigs = useMemo(() => {
    const q = (filter || "").trim().toLowerCase();
    if (!q)
    {
      return credentialConfigs;
    }

    return credentialConfigs.filter(({id, cfg, format}) => {
      if (id.toLowerCase().includes(q))
      {
        return true;
      }
      if ((format || "").toLowerCase().includes(q))
      {
        return true;
      }

      const displayName =
        cfg?.display?.[0]?.name ||
        cfg?.display?.name ||
        cfg?.name ||
        cfg?.credential_name ||
        "";
      if (String(displayName).toLowerCase().includes(q))
      {
        return true;
      }

      const scope = cfg?.scope;
      if (String(scope || "").toLowerCase().includes(q))
      {
        return true;
      }

      return JSON.stringify(cfg).toLowerCase().includes(q);
    });
  }, [credentialConfigs, filter]);

  const highlight = (text) => {
    const q = (filter || "").trim();
    if (!q)
    {
      return <>{text}</>;
    }
    const s = String(text);
    const idx = s.toLowerCase().indexOf(q.toLowerCase());
    if (idx < 0)
    {
      return <>{text}</>;
    }
    return (
      <>
        {s.slice(0, idx)}
        <mark>{s.slice(idx, idx + q.length)}</mark>
        {s.slice(idx + q.length)}
      </>
    );
  };

  const toggleExpanded = (id) => {
    setExpandedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id))
      {
        next.delete(id);
      }
      else
      {
        next.add(id);
      }
      return next;
    });
  };

  const copyToClipboard = async (value) => {
    try
    {
      await navigator.clipboard.writeText(String(value ?? ""));
    }
    catch (e)
    {
    }
  };

  // Scrollable JSON panel that matches the deep-link display style (OfferEditor)
  const jsonPanelStyle = {
    maxHeight: "420px",
    overflow: "auto",
    fontFamily: "monospace",
    whiteSpace: "pre",
  };

  const jsonPanelStyleSm = {
    ...jsonPanelStyle,
    maxHeight: "320px",
  };

  return (
    <div className="oidc-credential-offer-editor">
      <Card className="shadow-sm">
        <Card.Header className="d-flex justify-content-between align-items-center">
          <div className="d-flex align-items-center gap-2">
            <strong>{title}</strong>
            <Badge bg="info">read-only</Badge>
            {issuer ? <Badge bg="secondary">issuer</Badge> : <Badge bg="warning">missing issuer</Badge>}
          </div>

          <Button
            size="sm"
            variant="outline-light"
            onClick={() => copyToClipboard(JSON.stringify(meta ?? {}, null, 2))}
            title="Copy metadata JSON"
            disabled={!meta}
          >
            <AiOutlineCopy className="me-1" />
            Copy JSON
          </Button>
        </Card.Header>

        <Card.Body>
          {!meta ? (
            <div className="text-muted">No metadata available.</div>
          ) : (
            <>
              {/* Top cards */}
              <Row className="g-3 mb-3">
                <Col lg={6}>
                  <Card className="shadow-sm">
                    <Card.Body>
                      <div className="d-flex justify-content-between align-items-start">
                        <div>
                          <div className="fw-bold">Credential Issuer</div>
                          <div className="text-muted" style={{fontSize: 13}}>
                            Primary identifier
                          </div>
                        </div>
                        <Badge bg={issuer ? "secondary" : "warning"}>{issuer ? "ok" : "missing"}</Badge>
                      </div>

                      <Form.Control
                        readOnly
                        value={issuer || "(not provided)"}
                        className="mt-2"
                        onFocus={(e) => e.target.select()}
                      />

                      <div className="mt-2 d-flex justify-content-end">
                        <Button
                          size="sm"
                          variant="outline-light"
                          onClick={() => copyToClipboard(issuer)}
                          disabled={!issuer}
                        >
                          <AiOutlineCopy className="me-1" />
                          Copy
                        </Button>
                      </div>
                    </Card.Body>
                  </Card>
                </Col>

                <Col lg={6}>
                  <Card className="shadow-sm">
                    <Card.Body>
                      <div className="d-flex justify-content-between align-items-start">
                        <div>
                          <div className="fw-bold">Authorization Servers</div>
                          <div className="text-muted" style={{fontSize: 13}}>
                            {authServers.length ? `${authServers.length} configured` : "No authorization_servers in metadata"}
                          </div>
                        </div>
                        <Badge bg="secondary">{authServers.length}</Badge>
                      </div>

                      <ListGroup variant="flush" className="mt-2">
                        {(authServers.length ? authServers : ["(none)"]).map((v) => {
                          const isEmpty = authServers.length === 0;
                          return (
                            <ListGroup.Item
                              key={isEmpty ? "empty" : v}
                              className="px-0 d-flex justify-content-between align-items-start"
                              style={{gap: 12, background: "transparent"}}
                            >
                              <div
                                className={`text-break ${isEmpty ? "text-muted" : ""}`}
                                style={{fontFamily: isEmpty ? "inherit" : "monospace", fontSize: 12}}
                              >
                                {v}
                              </div>
                              {!isEmpty ? (
                                <Button size="sm" variant="outline-light" onClick={() => copyToClipboard(v)}
                                        title="Copy">
                                  <AiOutlineCopy />
                                </Button>
                              ) : null}
                            </ListGroup.Item>
                          );
                        })}
                      </ListGroup>
                    </Card.Body>
                  </Card>
                </Col>
              </Row>

              {/* Endpoints + Filter */}
              <Row className="g-3 mb-3">
                <Col lg={6}>
                  <Card className="shadow-sm">
                    <Card.Body>
                      <div className="d-flex justify-content-between align-items-start mb-2">
                        <div>
                          <div className="fw-bold">Endpoints</div>
                          <div className="text-muted" style={{fontSize: 13}}>
                            Extracted from metadata
                          </div>
                        </div>
                        <Badge bg="secondary">{Object.keys(endpoints).length}</Badge>
                      </div>

                      <ListGroup variant="flush">
                        {Object.entries(endpoints).map(([key, url]) => (
                          <ListGroup.Item
                            key={key}
                            className="px-0 d-flex justify-content-between align-items-start"
                            style={{gap: 12, background: "transparent"}}
                          >
                            <div>
                              <div className="fw-bold" style={{fontSize: 13}}>
                                {key}
                              </div>
                              <div className="text-break" style={{fontFamily: "monospace", fontSize: 12}}>
                                {url}
                              </div>
                            </div>
                            <Button size="sm" variant="outline-light" onClick={() => copyToClipboard(url)} title="Copy">
                              <AiOutlineCopy />
                            </Button>
                          </ListGroup.Item>
                        ))}
                        {Object.keys(endpoints).length === 0 && (
                          <ListGroup.Item className="px-0 text-muted" style={{background: "transparent"}}>
                            (no endpoints detected)
                          </ListGroup.Item>
                        )}
                      </ListGroup>
                    </Card.Body>
                  </Card>
                </Col>

                <Col lg={6}>
                  <Card className="shadow-sm">
                    <Card.Body>
                      <div className="d-flex justify-content-between align-items-start mb-2">
                        <div>
                          <div className="fw-bold">Credential configurations</div>
                          <div className="text-muted" style={{fontSize: 13}}>
                            Filter + expand details
                          </div>
                        </div>
                        <Badge bg="secondary">{credentialConfigs.length}</Badge>
                      </div>

                      <InputGroup>
                        <InputGroup.Text className="bg-dark text-light">
                          <AiOutlineSearch />
                        </InputGroup.Text>

                        <Form.Control
                          className="bg-dark text-light"
                          placeholder="Filter by id, format, display name, scope…"
                          value={filter}
                          onChange={(e) => setFilter(e.target.value)}
                        />

                        {filter ? (
                          <Button variant="outline-light" onClick={() => setFilter("")}>
                            Clear
                          </Button>
                        ) : null}
                      </InputGroup>

                      <div className="text-muted mt-2" style={{fontSize: 12}}>
                        Click a row to expand. “Copy JSON” copies that specific config.
                      </div>
                    </Card.Body>
                  </Card>
                </Col>
              </Row>

              {/* Config table */}
              <Card className="shadow-sm">
                <Card.Body>
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <div className="fw-bold">credential_configurations_supported</div>
                    <div className="d-flex gap-2">
                      <Badge bg="secondary">{filteredCredentialConfigs.length} shown</Badge>
                      <Button
                        size="sm"
                        variant="outline-light"
                        onClick={() => setExpandedIds(new Set(filteredCredentialConfigs.map((x) => x.id)))}
                      >
                        Expand all
                      </Button>
                      <Button size="sm" variant="outline-light" onClick={() => setExpandedIds(new Set())}>
                        Collapse all
                      </Button>
                    </div>
                  </div>

                  <div style={{overflowX: "auto"}}>
                    <Table hover responsive variant="dark" className="mb-0">
                      <thead>
                        <tr>
                          <th style={{width: "40%"}}>ID</th>
                          <th style={{width: "18%"}}>Format</th>
                          <th style={{width: "32%"}}>Display / Name</th>
                          <th style={{width: "10%"}} className="text-end">
                            Actions
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {filteredCredentialConfigs.map(({id, cfg, format}) => {
                          const isOpen = expandedIds.has(id);
                          const displayName =
                            cfg?.display?.[0]?.name ||
                            cfg?.display?.name ||
                            cfg?.name ||
                            cfg?.credential_name ||
                            "(no display)";

                          return (
                            <React.Fragment key={id}>
                              <tr style={{cursor: "pointer"}} onClick={() => toggleExpanded(id)}
                                  title="Click to expand">
                                <td className="text-break" style={{fontFamily: "monospace"}}>
                                  {highlight(id)}{" "}
                                  {isOpen ? (
                                    <Badge bg="info" className="ms-2">
                                      expanded
                                    </Badge>
                                  ) : null}
                                </td>
                                <td>{format ? <Badge bg="secondary">{highlight(format)}</Badge> :
                                  <Badge bg="warning">unknown</Badge>}</td>
                                <td className="text-break">{highlight(displayName)}</td>
                                <td className="text-end" onClick={(e) => e.stopPropagation()}>
                                  <Button
                                    size="sm"
                                    variant="outline-light"
                                    className="me-2"
                                    onClick={() => copyToClipboard(id)}
                                    title="Copy ID"
                                  >
                                    <AiOutlineCopy />
                                  </Button>
                                  <Button
                                    size="sm"
                                    variant="outline-light"
                                    onClick={() => copyToClipboard(JSON.stringify(cfg, null, 2))}
                                    title="Copy config JSON"
                                  >
                                    Copy JSON
                                  </Button>
                                </td>
                              </tr>

                              {isOpen && (
                                <tr>
                                  <td colSpan={4}>
                                    <Row className="g-3">
                                      <Col lg={6}>
                                        <MiniKV title="Scope" value={cfg?.scope} onCopy={copyToClipboard} />
                                        <MiniKV title="VCT / Type" value={cfg?.vct || cfg?.type}
                                                onCopy={copyToClipboard} />
                                        <MiniKV
                                          title="Credential endpoint"
                                          value={cfg?.credential_endpoint || meta?.credential_endpoint}
                                          onCopy={copyToClipboard}
                                        />
                                        <MiniKV
                                          title="Issuer display"
                                          value={meta?.display?.[0]?.name || meta?.display?.name}
                                          onCopy={copyToClipboard}
                                        />
                                      </Col>

                                      <Col lg={6}>
                                        <Accordion className="deep-link-accordion">
                                          <Accordion.Item eventKey="json">
                                            <Accordion.Header>Raw configuration JSON</Accordion.Header>
                                            <Accordion.Body>
                                              <div
                                                className="deep-link-full"
                                                style={jsonPanelStyleSm}
                                                onDoubleClick={() => copyToClipboard(JSON.stringify(cfg, null, 2))}
                                                title="Double-click to copy JSON"
                                              >
                                                {JSON.stringify(cfg, null, 2)}
                                              </div>
                                            </Accordion.Body>
                                          </Accordion.Item>
                                        </Accordion>
                                      </Col>
                                    </Row>
                                  </td>
                                </tr>
                              )}
                            </React.Fragment>
                          );
                        })}

                        {filteredCredentialConfigs.length === 0 && (
                          <tr>
                            <td colSpan={4} className="text-muted">
                              No credential configurations match your filter.
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </Table>
                  </div>
                </Card.Body>
              </Card>

              {/* x5c download section */}
              <Card className="shadow-sm mb-3">
                <Card.Header className="d-flex justify-content-between align-items-center">
                  <div className="d-flex align-items-center gap-2">
                    <AiOutlineSafetyCertificate />
                    <strong>Certificates (x5c)</strong>
                    <Badge bg={x5cChains.length ? "secondary" : "warning"}>{x5cChains.length}</Badge>
                  </div>
                </Card.Header>

                <Card.Body>
                  {x5cChains.length === 0 ? (
                    <div className="text-muted">
                      No <code>x5c</code> entries found in the provided metadata object.
                      <div className="text-muted" style={{fontSize: 12, marginTop: 6}}>
                        (Common locations are <code>jwks.keys[].x5c</code> or JWT header <code>x5c</code> in{" "}
                        <code>signed_metadata</code>.)
                      </div>
                    </div>
                  ) : (
                    <Accordion className="deep-link-accordion">
                      {x5cChains.map((entry, idx) => (
                        <Accordion.Item key={idx} eventKey={String(idx)}>
                          <Accordion.Header>
                            <X5cPathHeader path={entry.path} index={entry.index} kid={entry.kid} label={entry.label} />
                          </Accordion.Header>
                          <Accordion.Body>
                            <X5cCertificateDownloadPanel
                              x5c={entry.x5c}
                              filenamePrefix={entry.filenamePrefix || `x5c-${idx + 1}`}
                            />
                          </Accordion.Body>
                        </Accordion.Item>
                      ))}
                    </Accordion>
                  )}
                </Card.Body>
              </Card>

              {/* Raw metadata */}
              <Card className="shadow-sm mt-3">
                <Card.Body>
                  <Accordion className="deep-link-accordion">
                    <Accordion.Item eventKey="raw">
                      <Accordion.Header>Raw OID4VCI metadata JSON</Accordion.Header>
                      <Accordion.Body>
                        <div
                          className="deep-link-full"
                          style={jsonPanelStyle}
                          onDoubleClick={() => copyToClipboard(JSON.stringify(meta, null, 2))}
                          title="Double-click to copy JSON"
                        >
                          {JSON.stringify(meta, null, 2)}
                        </div>
                      </Accordion.Body>
                    </Accordion.Item>
                  </Accordion>
                </Card.Body>
              </Card>
            </>
          )}
        </Card.Body>
      </Card>
    </div>
  );
}

/**
 * UI: Downloads x5c entries as CER (DER) or PEM
 * - No new CSS required
 */
function X5cCertificateDownloadPanel({x5c, filenamePrefix = "x5c"})
{
  const chain = Array.isArray(x5c) ? x5c.filter(Boolean) : [];
  const [format, setFormat] = useState("cer"); // "cer" | "pem"

  const items = useMemo(() => {
    return chain.map((b64, index) => ({
      index,
      role: index === 0 ? "Leaf" : index === chain.length - 1 ? "Root" : "Intermediate",
      b64,
    }));
  }, [chain]);

  const canDownload = chain.length > 0;

  if (!canDownload)
  {
    return <div className="text-muted">No x5c chain present.</div>;
  }

  return (
    <Card className="shadow-sm">
      <Card.Header className="d-flex justify-content-between align-items-center">
        <div className="d-flex align-items-center gap-2">
          <AiOutlineSafetyCertificate />
          <strong>X.509 certificate chain</strong>
          <Badge bg="secondary">{chain.length}</Badge>
        </div>

        <div className="d-flex align-items-center gap-2">
          <ButtonGroup size="sm" aria-label="Format toggle">
            <Button
              variant={format === "cer" ? "warning" : "outline-light"}
              onClick={() => setFormat("cer")}
              title="DER encoded .cer"
            >
              CER
            </Button>
            <Button
              variant={format === "pem" ? "warning" : "outline-light"}
              onClick={() => setFormat("pem")}
              title="PEM encoded .pem"
            >
              PEM
            </Button>
          </ButtonGroup>

          <Button
            size="sm"
            variant="outline-light"
            onClick={() => {
              if (format === "cer")
              {
                downloadAllAsCer(chain, filenamePrefix);
              }
              else
              {
                downloadChainAsPem(chain, `${filenamePrefix}-chain.pem`);
              }
            }}
          >
            <AiOutlineDownload className="me-1" />
            Download {format === "cer" ? "all" : "chain"}
          </Button>
        </div>
      </Card.Header>

      <ListGroup variant="flush">
        {items.map(({index, role, b64}) => (
          <ListGroup.Item key={index} className="d-flex justify-content-between align-items-center" style={{gap: 12}}>
            <div>
              <div className="fw-bold">
                {role} certificate{" "}
                <Badge bg="info" className="ms-2">
                  #{index + 1}
                </Badge>
              </div>
              <div className="text-muted" style={{fontSize: 12}}>
                {format === "cer" ? "DER (.cer)" : "PEM (.pem)"}
              </div>
            </div>

            <Button
              size="sm"
              variant="outline-light"
              onClick={() => {
                if (format === "cer")
                {
                  downloadSingleAsCer(b64,
                                      `${filenamePrefix}-${String(index + 1)
                                        .padStart(2, "0")}-${role.toLowerCase()}.cer`);
                }
                else
                {
                  downloadSingleAsPem(b64,
                                      `${filenamePrefix}-${String(index + 1)
                                        .padStart(2, "0")}-${role.toLowerCase()}.pem`);
                }
              }}
              title={format === "cer" ? "Download as .cer" : "Download as .pem"}
            >
              <AiOutlineDownload />
            </Button>
          </ListGroup.Item>
        ))}
      </ListGroup>
    </Card>
  );
}

function MiniKV({title, value, onCopy})
{
  const v = value == null || value === "" ? "(not provided)" : String(value);
  return (
    <div className="mb-2">
      <div className="d-flex justify-content-between align-items-center">
        <div className="fw-bold" style={{fontSize: 12}}>
          {title}
        </div>
        <Button
          size="sm"
          variant="outline-light"
          onClick={() => onCopy(v)}
          disabled={v === "(not provided)"}
          title="Copy"
        >
          <AiOutlineCopy />
        </Button>
      </div>

      <Form.Control
        readOnly
        value={v}
        className="mt-1"
        style={{fontFamily: "monospace", fontSize: 12}}
        onFocus={(e) => e.target.select()}
      />
    </div>
  );
}

/* ------------------------------ x5c extraction ------------------------------ */

function extractX5cChains(meta)
{
  if (!meta || typeof meta !== "object")
  {
    return [];
  }

  const found = [];

  // helper: collect jwks.keys[].x5c from an object at a path
  const collectFromJwks = (jwks, pathParts, labelPrefix, filenamePrefixPrefix) => {
    if (!jwks || typeof jwks !== "object" || !Array.isArray(jwks.keys))
    {
      return;
    }

    jwks.keys.forEach((k, idx) => {
      if (Array.isArray(k?.x5c) && k.x5c.length > 0)
      {
        found.push({
                     label: `${labelPrefix}.keys[${idx}]${k.kid ? ` kid=${k.kid}` : ""}`,
                     path: [...pathParts, "keys"],
                     index: idx,
                     kid: k.kid,
                     filenamePrefix: `${filenamePrefixPrefix}-${k.kid || idx + 1}`,
                     x5c: k.x5c,
                   });
      }
    });
  };

  // 1) credential_request_encryption.jwks.keys[].x5c  ✅ (DEIN FALL)
  collectFromJwks(
    meta?.credential_request_encryption?.jwks,
    ["credential_request_encryption", "jwks"],
    "credential_request_encryption.jwks",
    "req-enc-jwks"
  );

  // 2) credential_response_encryption.jwks.keys[].x5c (optional / future-proof)
  collectFromJwks(
    meta?.credential_response_encryption?.jwks,
    ["credential_response_encryption", "jwks"],
    "credential_response_encryption.jwks",
    "resp-enc-jwks"
  );

  // 3) plain jwks.keys[].x5c (falls ein Issuer das so liefert)
  collectFromJwks(meta?.jwks, "jwks", "jwks");

  // 4) authorization_server_metadata.jwks.keys[].x5c (nested)
  collectFromJwks(
    meta?.authorization_server_metadata?.jwks,
    ["authorization_server_metadata", "jwks"],
    "authorization_server_metadata.jwks",
    "as-jwks"
  );

  // 5) oauth_authorization_server_metadata.jwks.keys[].x5c (alternative nesting)
  collectFromJwks(
    meta?.oauth_authorization_server_metadata?.jwks,
    ["oauth_authorization_server_metadata", "jwks"],
    "oauth_authorization_server_metadata.jwks",
    "oauth-as-jwks"
  );

  // 6) signed_metadata JWT header x5c
  const signedMetadataJwt = typeof meta?.signed_metadata === "string" ? meta.signed_metadata : null;
  if (signedMetadataJwt)
  {
    const header = safeDecodeJwtHeader(signedMetadataJwt);
    if (header && Array.isArray(header.x5c) && header.x5c.length > 0)
    {
      found.push({
                   label: `signed_metadata (JWT header)`,
                   filenamePrefix: `signed-metadata`,
                   x5c: header.x5c,
                 });
    }
  }

  // De-dup
  const seen = new Set();
  return found.filter((e) => {
    const key = `${e.label}::${(e.x5c || []).join("|")}`;
    if (seen.has(key))
    {
      return false;
    }
    seen.add(key);
    return true;
  });
}


function safeDecodeJwtHeader(jwt)
{
  try
  {
    const parts = String(jwt).split(".");
    if (parts.length < 2)
    {
      return null;
    }
    const h = parts[0];
    const b64 = h.replace(/-/g, "+").replace(/_/g, "/") + "=".repeat((4 - (h.length % 4)) % 4);
    return JSON.parse(atob(b64));
  }
  catch (e)
  {
    return null;
  }
}

/* ------------------------- download helpers: CER (DER) ------------------------- */

function base64ToUint8Array(b64)
{
  const normalized = String(b64).replace(/-/g, "+").replace(/_/g, "/");
  const padded = normalized + "=".repeat((4 - (normalized.length % 4)) % 4);

  const bin = atob(padded);
  const bytes = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++)
  {
    bytes[i] = bin.charCodeAt(i);
  }
  return bytes;
}

function downloadBlob(blob, filename)
{
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

function downloadSingleAsCer(b64, filename)
{
  const bytes = base64ToUint8Array(b64);
  const blob = new Blob([bytes], {type: "application/pkix-cert"});
  downloadBlob(blob, filename);
}

function downloadAllAsCer(x5c, prefix)
{
  (x5c || []).forEach((b64, i) => {
    downloadSingleAsCer(b64, `${prefix}-${String(i + 1).padStart(2, "0")}.cer`);
  });
}

/* ------------------------------ download helpers: PEM ------------------------------ */

function x5cEntryToPem(b64)
{
  const normalized = String(b64).replace(/-/g, "+").replace(/_/g, "/");
  const padded = normalized + "=".repeat((4 - (normalized.length % 4)) % 4);
  const lines = padded.match(/.{1,64}/g) || [];
  return `-----BEGIN CERTIFICATE-----\n${lines.join("\n")}\n-----END CERTIFICATE-----\n`;
}

function downloadSingleAsPem(b64, filename)
{
  const pem = x5cEntryToPem(b64);
  const blob = new Blob([pem], {type: "application/x-pem-file"});
  downloadBlob(blob, filename);
}

function downloadChainAsPem(x5c, filename)
{
  const pem = (x5c || []).map(x5cEntryToPem).join("\n");
  const blob = new Blob([pem], {type: "application/x-pem-file"});
  downloadBlob(blob, filename);
}

/* ------------------------------ endpoints extraction ------------------------------ */

function extractEndpoints(meta)
{
  if (!meta || typeof meta !== "object")
  {
    return {};
  }

  const candidates = [
    "credential_endpoint",
    "batch_credential_endpoint",
    "deferred_credential_endpoint",
    "notification_endpoint",
    "pushed_authorization_request_endpoint",
    "authorization_endpoint",
    "token_endpoint",
    "jwks_uri",
    "signed_metadata",
    "nonce_endpoint",
  ];

  const out = {};
  for (const k of candidates)
  {
    const v = meta?.[k];
    if (typeof v === "string" && v.trim())
    {
      out[k] = v.trim();
    }
  }

  const nested = meta?.authorization_server_metadata || meta?.oauth_authorization_server_metadata;
  if (nested && typeof nested === "object")
  {
    for (const k of ["authorization_endpoint", "token_endpoint", "jwks_uri", "pushed_authorization_request_endpoint"])
    {
      const v = nested?.[k];
      if (typeof v === "string" && v.trim())
      {
        out[`auth_server.${k}`] = v.trim();
      }
    }
  }

  return out;
}
