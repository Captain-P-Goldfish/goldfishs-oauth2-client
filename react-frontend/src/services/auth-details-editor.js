import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
  Accordion,
  Alert,
  Badge,
  Button,
  Card,
  Col,
  Form,
  InputGroup,
  Row,
} from "react-bootstrap";

/* =========================
 Helpers
 ========================= */

function isPlainObject(x) {
  return x !== null && typeof x === "object" && !Array.isArray(x);
}

function toSearchParams(q) {
  const s = (q ?? "").toString();
  return new URLSearchParams(s.startsWith("?") ? s.slice(1) : s);
}

function fromSearchParams(sp) {
  return sp.toString();
}

function safeDecodeURIComponent(s) {
  try {
    return decodeURIComponent(s);
  } catch {
    return null;
  }
}

function safeJsonParse(s) {
  try {
    return JSON.parse(s);
  } catch {
    return null;
  }
}

function onEnter(e, fn) {
  if (e.key === "Enter") {
    e.preventDefault();
    e.stopPropagation();
    fn();
  }
}

function splitCsv(value) {
  const raw = (value ?? "").trim();
  if (!raw) return [];
  return raw
    .split(",")
    .map((x) => x.trim())
    .filter((x) => x.length > 0);
}

function joinCsv(arr) {
  if (!Array.isArray(arr) || arr.length === 0) return "";
  return arr.join(", ");
}

/* =========================
 Parse / Write authorization_details
 ========================= */

function parseAuthorizationDetailsFromQuery(queryString) {
  const sp = toSearchParams(queryString);
  const raw = sp.get("authorization_details");
  if (!raw) return [];

  // URLSearchParams.get decodes one layer. Also try decode once/twice for legacy inputs.
  const candidates = [
    raw,
    safeDecodeURIComponent(raw),
    safeDecodeURIComponent(safeDecodeURIComponent(raw)),
  ].filter((x) => typeof x === "string" && x.length > 0);

  for (const c of candidates) {
    const parsed = safeJsonParse(c);
    if (Array.isArray(parsed)) return parsed.filter((x) => isPlainObject(x));
  }

  return [];
}

function writeAuthorizationDetailsToQuery(queryString, authDetailsArray) {
  const sp = toSearchParams(queryString);

  const cleaned = Array.isArray(authDetailsArray)
    ? authDetailsArray.filter((x) => isPlainObject(x))
    : [];

  if (cleaned.length === 0) {
    sp.delete("authorization_details");
    return fromSearchParams(sp);
  }

  // Single-URL-encoding: pass raw JSON; URLSearchParams encodes once.
  sp.set("authorization_details", JSON.stringify(cleaned));
  return fromSearchParams(sp);
}

/* =========================
 Access workflow details
 ========================= */

function getQueryParameters(workflowDetails) {
  return workflowDetails?.authCodeParameters?.queryParameters ?? "";
}

/* =========================
 Main Component
 ========================= */

export function AuthorizationDetailsEditor({
                                             workflowDetails,
                                             handleChange,
                                             fieldPath = "authCodeParameters.queryParameters",
                                           }) {
  const query = getQueryParameters(workflowDetails);


  // IMPORTANT: always write against the latest query string (prevents resurrecting removed params)
  const latestQueryRef = useRef("");
  latestQueryRef.current = query; // IMPORTANT: update during render

  const [openKey, setOpenKey] = useState(null); // outer accordion collapsed by default
  const [items, setItems] = useState(() => parseAuthorizationDetailsFromQuery(query));

  // react-bootstrap Accordion expects string event keys (or null)
  const [activeItemKey, setActiveItemKey] = useState(null); // string or null
  const [parseWarning, setParseWarning] = useState(null);

  // IMPORTANT: depend ONLY on query, otherwise Add gets “undone” by parsing stale query
  useEffect(() => {
    try {
      const next = parseAuthorizationDetailsFromQuery(query);
      setItems(next);
      setParseWarning(null);

      // keep active key only if still valid
      setActiveItemKey((prev) => {
        if (prev === null) return null;
        const idx = Number(prev);
        if (!Number.isFinite(idx) || next[idx] === undefined) return null;
        return prev;
      });
    } catch (e) {
      setParseWarning(String(e?.message ?? e));
      setItems([]);
      setActiveItemKey(null);
    }
  }, [query]);

  const commit = useCallback(
    (nextItems) => {
      setItems(nextItems);

      const baseQuery = latestQueryRef.current || "";
      const nextQuery = writeAuthorizationDetailsToQuery(baseQuery, nextItems);

      // IMPORTANT: update immediately to avoid resurrecting old params
      latestQueryRef.current = nextQuery;

      handleChange(fieldPath, nextQuery);
    },
    [handleChange, fieldPath]
  );


  const updateItem = useCallback(
    (index, updater) => {
      const next = structuredClone(items);
      const current = isPlainObject(next[index]) ? next[index] : {};
      next[index] = updater(current);
      commit(next);
    },
    [items, commit]
  );

  const removeItem = useCallback(
    (index) => {
      const next = items.slice();
      next.splice(index, 1);
      commit(next);

      setActiveItemKey((prev) => {
        if (prev === null) return null;
        const prevIdx = Number(prev);
        if (!Number.isFinite(prevIdx)) return null;
        if (prevIdx === index) return null;
        if (prevIdx > index) return String(prevIdx - 1);
        return prev;
      });
    },
    [items, commit]
  );

  const addNew = useCallback(() => {
    // Do NOT include credential_configuration_id unless user enters a value (fix #1)
    const newEntry = { type: "openid_credential" };

    const next = items.concat([newEntry]);
    commit(next);

    // open outer accordion + open the newly added item
    setOpenKey("authorization-details-editor");
    setActiveItemKey(String(next.length - 1));
  }, [items, commit]);

  const jsonPreview = useMemo(() => items, [items]);
  const isActive = items.length > 0;

  return (
    <div className="auth-details-editor">
      <Accordion activeKey={openKey} onSelect={(k) => setOpenKey(k)}>
        <Accordion.Item eventKey="authorization-details-editor">
          <Accordion.Header>
            <strong>OID4VCI authorization_details editor</strong>
            <Badge bg="warning" text="dark" className="ms-2">
              RAR
            </Badge>
            {isActive ? (
              <Badge bg="info" className="ms-2">
                active
              </Badge>
            ) : (
              <Badge bg="secondary" className="ms-2">
                omitted
              </Badge>
            )}
          </Accordion.Header>

          <Accordion.Body>
            {parseWarning && (
              <Alert variant="warning">
                Could not parse <code>authorization_details</code>: {parseWarning}
              </Alert>
            )}

            <Row className="g-3">
              {/* LEFT: editor */}
              <Col lg={7}>
                <Card bg="dark" border="warning" className="mb-3">
                  <Card.Header className="d-flex align-items-center justify-content-between">
                    <strong>Requested credentials</strong>
                    <Button variant="warning" type="button" onClick={addNew}>
                      Add
                    </Button>
                  </Card.Header>

                  <Card.Body>
                    {items.length === 0 ? (
                      <Alert variant="secondary" className="mb-0">
                        No <code>authorization_details</code> configured.
                      </Alert>
                    ) : (
                      <Accordion activeKey={activeItemKey} onSelect={(k) => setActiveItemKey(k)}>
                        {items.map((entry, idx) => (
                          <AuthorizationDetailItem
                            key={idx}
                            eventKey={String(idx)}
                            entry={entry}
                            onRemove={() => removeItem(idx)}
                            onUpdate={(updater) => updateItem(idx, updater)}
                          />
                        ))}
                      </Accordion>
                    )}
                  </Card.Body>
                </Card>
              </Col>

              {/* RIGHT: JSON preview always visible */}
              <Col lg={5}>
                <Card bg="dark" border="warning">
                  <Card.Header className="d-flex align-items-center justify-content-between">
                    <strong>authorization_details JSON</strong>
                    {isActive ? (
                      <Badge bg="info">active</Badge>
                    ) : (
                      <Badge bg="secondary">omitted</Badge>
                    )}
                  </Card.Header>
                  <Card.Body>
                    <pre style={{ fontSize: 12, marginBottom: 0, whiteSpace: "pre-wrap" }}>
                      {JSON.stringify(jsonPreview, null, 2)}
                    </pre>
                  </Card.Body>
                </Card>
              </Col>
            </Row>
          </Accordion.Body>
        </Accordion.Item>
      </Accordion>
    </div>
  );
}

/* =========================
 Item Editor
 ========================= */

function AuthorizationDetailItem({ eventKey, entry, onRemove, onUpdate }) {
  const type = typeof entry.type === "string" ? entry.type : "";
  const cci = typeof entry.credential_configuration_id === "string" ? entry.credential_configuration_id : "";
  const format = typeof entry.format === "string" ? entry.format : "";

  const typesArr = Array.isArray(entry.types) ? entry.types : [];
  const locationsArr = Array.isArray(entry.locations) ? entry.locations : [];

  // FIX #2: raw text state so commas don’t get “eaten” by instant parsing
  const [typesText, setTypesText] = useState(joinCsv(typesArr));
  const [locationsText, setLocationsText] = useState(joinCsv(locationsArr));

  useEffect(() => {
    setTypesText(joinCsv(typesArr));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [entry.types]);

  useEffect(() => {
    setLocationsText(joinCsv(locationsArr));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [entry.locations]);

  const customEntries = Object.entries(entry).filter(
    ([k]) => !["type", "credential_configuration_id", "format", "locations", "types"].includes(k)
  );

  const [innerActiveKey, setInnerActiveKey] = useState("main");

  return (
    <Accordion.Item eventKey={eventKey}>
      <Accordion.Header>
        <span className="d-inline-flex align-items-center gap-2 flex-wrap">
          <strong>{cci?.trim() ? cci : "(new openid_credential request)"}</strong>
          {type ? <Badge bg="secondary">{type}</Badge> : <Badge bg="secondary">no type</Badge>}
        </span>

        <Button
          size="sm"
          variant="outline-warning"
          className="ms-auto"
          type="button"
          onClick={(e) => {
            e.preventDefault();
            e.stopPropagation();
            onRemove();
          }}
        >
          Remove
        </Button>
      </Accordion.Header>

      <Accordion.Body>
        <Accordion activeKey={innerActiveKey} onSelect={(k) => setInnerActiveKey(k)}>
          <Accordion.Item eventKey="main">
            <Accordion.Header>Main</Accordion.Header>
            <Accordion.Body>
              <Row className="g-3">
                <Col md={6}>
                  <Form.Label className="mb-1">type</Form.Label>
                  <Form.Control
                    value={type}
                    placeholder="openid_credential"
                    onChange={(e) => {
                      const v = e.target.value;
                      onUpdate((obj) => {
                        const next = { ...obj };
                        if (v && v.trim()) next.type = v;
                        else delete next.type;
                        return next;
                      });
                    }}
                    onKeyDown={(e) => onEnter(e, () => e.currentTarget.blur())}
                  />
                </Col>

                <Col md={6}>
                  <Form.Label className="mb-1">credential_configuration_id (optional)</Form.Label>
                  <Form.Control
                    value={cci}
                    placeholder="e.g. UniversityDegreeCredential"
                    onChange={(e) => {
                      const v = e.target.value;
                      onUpdate((obj) => {
                        const next = { ...obj };
                        // FIX #1: omit field when empty
                        if (v && v.trim()) next.credential_configuration_id = v;
                        else delete next.credential_configuration_id;
                        return next;
                      });
                    }}
                    onKeyDown={(e) => onEnter(e, () => e.currentTarget.blur())}
                  />
                </Col>

                <Col md={6}>
                  <Form.Label className="mb-1">format (optional)</Form.Label>
                  <Form.Control
                    value={format}
                    placeholder="e.g. jwt_vc, sd_jwt_vc, mso_mdoc"
                    onChange={(e) => {
                      const v = e.target.value;
                      onUpdate((obj) => {
                        const next = { ...obj };
                        if (v && v.trim()) next.format = v;
                        else delete next.format;
                        return next;
                      });
                    }}
                    onKeyDown={(e) => onEnter(e, () => e.currentTarget.blur())}
                  />
                </Col>

                <Col md={6}>
                  <Form.Label className="mb-1">types (optional, CSV)</Form.Label>
                  <Form.Control
                    value={typesText}
                    placeholder="e.g. VerifiableCredential, MyCredentialType"
                    onChange={(e) => setTypesText(e.target.value)}
                    onBlur={() => {
                      const arr = splitCsv(typesText);
                      onUpdate((obj) => {
                        const next = { ...obj };
                        if (arr.length > 0) next.types = arr;
                        else delete next.types;
                        return next;
                      });
                    }}
                    onKeyDown={(e) =>
                      onEnter(e, () => {
                        const arr = splitCsv(typesText);
                        onUpdate((obj) => {
                          const next = { ...obj };
                          if (arr.length > 0) next.types = arr;
                          else delete next.types;
                          return next;
                        });
                        e.currentTarget.blur();
                      })
                    }
                  />
                </Col>

                <Col md={12}>
                  <Form.Label className="mb-1">locations (optional, CSV)</Form.Label>
                  <Form.Control
                    value={locationsText}
                    placeholder="e.g. https://issuer.example, https://issuer.example/credential"
                    onChange={(e) => setLocationsText(e.target.value)}
                    onBlur={() => {
                      const arr = splitCsv(locationsText);
                      onUpdate((obj) => {
                        const next = { ...obj };
                        if (arr.length > 0) next.locations = arr;
                        else delete next.locations;
                        return next;
                      });
                    }}
                    onKeyDown={(e) =>
                      onEnter(e, () => {
                        const arr = splitCsv(locationsText);
                        onUpdate((obj) => {
                          const next = { ...obj };
                          if (arr.length > 0) next.locations = arr;
                          else delete next.locations;
                          return next;
                        });
                        e.currentTarget.blur();
                      })
                    }
                  />
                </Col>
              </Row>
            </Accordion.Body>
          </Accordion.Item>

          <Accordion.Item eventKey="custom">
            <Accordion.Header>
              Custom fields{" "}
              <span className="ms-2">
                {customEntries.length > 0 ? (
                  <Badge bg="dark">count:{customEntries.length}</Badge>
                ) : (
                  <Badge bg="secondary">none</Badge>
                )}
              </span>
            </Accordion.Header>

            <Accordion.Body>
              <CustomFieldsEditor
                entries={customEntries}
                onAdd={(key) => {
                  onUpdate((obj) => {
                    const k = (key ?? "").trim();
                    if (!k) return obj;
                    if (obj[k] !== undefined) return obj;
                    return { ...obj, [k]: "" };
                  });
                }}
                onRemove={(key) => {
                  onUpdate((obj) => {
                    const next = { ...obj };
                    delete next[key];
                    return next;
                  });
                }}
                onUpdateValue={(key, rawValue) => {
                  onUpdate((obj) => {
                    const next = { ...obj };
                    const parsed = safeJsonParse(rawValue);
                    next[key] = parsed !== null ? parsed : rawValue;
                    return next;
                  });
                }}
              />
            </Accordion.Body>
          </Accordion.Item>
        </Accordion>
      </Accordion.Body>
    </Accordion.Item>
  );
}

/* =========================
 Custom Fields
 ========================= */

function CustomFieldsEditor({ entries, onAdd, onRemove, onUpdateValue }) {
  const [newKey, setNewKey] = useState("");

  const add = useCallback(() => {
    const k = newKey.trim();
    if (!k) return;
    onAdd(k);
    setNewKey("");
  }, [newKey, onAdd]);

  return (
    <>
      <InputGroup className="mb-3">
        <Form.Control
          value={newKey}
          placeholder="custom key (e.g. credential_definition, proof, ...)"
          onChange={(e) => setNewKey(e.target.value)}
          onKeyDown={(e) => onEnter(e, add)}
        />
        <Button variant="outline-warning" type="button" onClick={add}>
          Add
        </Button>
      </InputGroup>

      {entries.length === 0 ? (
        <div className="text-muted" style={{ fontSize: 13 }}>
          No custom fields.
        </div>
      ) : (
        <div className="d-flex flex-column gap-2">
          {entries.map(([k, v]) => (
            <Row className="g-2" key={k}>
              <Col md={4}>
                <Form.Control value={k} readOnly />
              </Col>
              <Col md={6}>
                <Form.Control
                  defaultValue={typeof v === "string" ? v : JSON.stringify(v)}
                  placeholder='Value (string or JSON, e.g. {"a":1})'
                  onBlur={(e) => onUpdateValue(k, e.target.value)}
                  onKeyDown={(e) =>
                    onEnter(e, () => {
                      onUpdateValue(k, e.target.value);
                      e.currentTarget.blur();
                    })
                  }
                />
                <div className="text-muted" style={{ fontSize: 12 }}>
                  Saved on blur (or Enter). Valid JSON is stored as JSON, otherwise as string.
                </div>
              </Col>
              <Col md={2} className="d-grid">
                <Button variant="outline-warning" type="button" onClick={() => onRemove(k)}>
                  Remove
                </Button>
              </Col>
            </Row>
          ))}
        </div>
      )}
    </>
  );
}
