import React, { useCallback, useEffect, useMemo, useState } from "react";
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

function isClaimNull(entry) {
  return entry === null;
}

/**
 * Convert to an object ONLY when we actually want to write attributes.
 * If entry is null, we keep it null until the first attribute is set
 * OR until "null request" switch is turned off.
 */
function ensureClaimObject(entry) {
  if (entry === null) return {};
  if (!isPlainObject(entry)) return {};
  return entry;
}

function isEmptySection(section) {
  return !isPlainObject(section) || Object.keys(section).length === 0;
}

function getQueryParameters(workflowDetails) {
  return workflowDetails?.authCodeParameters?.queryParameters ?? "";
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

/* =========================
 Parse / Write claims
 ========================= */

function parseClaimsFromQuery(query) {
  const sp = toSearchParams(query);
  const raw = sp.get("claims");
  if (!raw) return { userinfo: {}, id_token: {} };

  // try raw, decode once, decode twice (covers previous double-encoding states)
  const candidates = [
    raw,
    safeDecodeURIComponent(raw),
    safeDecodeURIComponent(safeDecodeURIComponent(raw)),
  ].filter((x) => typeof x === "string" && x.length > 0);

  for (const c of candidates) {
    const parsed = safeJsonParse(c);
    if (isPlainObject(parsed)) {
      return {
        userinfo: isPlainObject(parsed.userinfo) ? parsed.userinfo : {},
        id_token: isPlainObject(parsed.id_token) ? parsed.id_token : {},
      };
    }
  }

  return { userinfo: {}, id_token: {} };
}

/**
 * Write back SINGLE-URL-encoded:
 * set raw JSON string and let URLSearchParams encode once.
 *
 * Also:
 * - omit empty userinfo / id_token
 * - if both empty => remove claims
 */
function writeClaimsToQuery(query, claims) {
  const sp = toSearchParams(query);

  const out = {};
  if (!isEmptySection(claims.userinfo)) out.userinfo = claims.userinfo;
  if (!isEmptySection(claims.id_token)) out.id_token = claims.id_token;

  if (Object.keys(out).length === 0) sp.delete("claims");
  else sp.set("claims", JSON.stringify(out));

  return fromSearchParams(sp);
}

/* =========================
 MAIN COMPONENT
 ========================= */

export function OidcClaimsEditor({
                                   workflowDetails,
                                   handleChange,
                                   fieldPath = "authCodeParameters.queryParameters",
                                 }) {
  const query = useMemo(() => getQueryParameters(workflowDetails), [workflowDetails]);
  const [claims, setClaims] = useState(() => parseClaimsFromQuery(query));
  const [parseWarning, setParseWarning] = useState(null);

  // collapsed by default
  const [openKey, setOpenKey] = useState(null);

  useEffect(() => {
    try {
      setClaims(parseClaimsFromQuery(query));
      setParseWarning(null);
    } catch (e) {
      setParseWarning(String(e?.message ?? e));
      setClaims({ userinfo: {}, id_token: {} });
    }
  }, [query]);

  const commit = useCallback(
    (next) => {
      setClaims(next);
      const nextQuery = writeClaimsToQuery(query, next);
      handleChange(fieldPath, nextQuery);
    },
    [query, handleChange, fieldPath]
  );

  const updateSection = useCallback(
    (key, fn) => {
      const next = structuredClone(claims);
      next[key] = isPlainObject(next[key]) ? next[key] : {};
      fn(next[key]);
      commit(next);
    },
    [claims, commit]
  );

  const jsonPreview = useMemo(() => {
    const out = {};
    if (!isEmptySection(claims.userinfo)) out.userinfo = claims.userinfo;
    if (!isEmptySection(claims.id_token)) out.id_token = claims.id_token;
    return out;
  }, [claims]);

  const claimsActive = Object.keys(jsonPreview).length > 0;

  return (
    <Accordion activeKey={openKey} onSelect={(k) => setOpenKey(k)}>
      <Accordion.Item eventKey="oidc-claims-editor">
        <Accordion.Header>
          <strong>OpenID Connect “claims” editor</strong>
          <Badge bg="warning" text="dark" className="ms-2">
            OIDC
          </Badge>
          {claimsActive ? (
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
          <div className="text-muted" style={{ fontSize: 12, marginBottom: 10 }}>
            Writes back to <code>{fieldPath}</code> on every change. (claims will be written single-URL-encoded)
          </div>

          {parseWarning && (
            <Alert variant="warning">
              Could not parse <code>claims</code> cleanly: {parseWarning}
            </Alert>
          )}

          <Row className="g-3">
            <Col lg={7}>
              <ClaimsSection
                title="UserInfo (userinfo)"
                sectionKey="userinfo"
                section={claims.userinfo}
                onChange={(fn) => updateSection("userinfo", fn)}
              />

              <ClaimsSection
                title="ID Token (id_token)"
                sectionKey="id_token"
                section={claims.id_token}
                onChange={(fn) => updateSection("id_token", fn)}
              />
            </Col>

            <Col lg={5}>
              <Card bg="dark" border="warning">
                <Card.Header className="d-flex justify-content-between align-items-center">
                  <strong>claims JSON</strong>
                  {claimsActive ? (
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
  );
}

/* =========================
 CLAIMS SECTION
 ========================= */

function ClaimsSection({ title, sectionKey, section, onChange }) {
  const [newName, setNewName] = useState("");

  const claimNames = useMemo(() => Object.keys(section || {}).sort(), [section]);

  const addClaim = useCallback(() => {
    const name = newName.trim();
    if (!name) return;
    onChange((s) => {
      if (s[name] === undefined) s[name] = null; // default new claim to null (requested w/o options)
    });
    setNewName("");
  }, [newName, onChange]);

  return (
    <Accordion defaultActiveKey={sectionKey} alwaysOpen className="mb-3">
      <Accordion.Item eventKey={sectionKey}>
        <Accordion.Header>
          {title}{" "}
          <span className="ms-2">
            {claimNames.length === 0 ? (
              <Badge bg="secondary">empty</Badge>
            ) : (
              <Badge bg="info">count:{claimNames.length}</Badge>
            )}
          </span>
        </Accordion.Header>

        <Accordion.Body>
          <InputGroup className="mb-3">
            <Form.Control
              placeholder="Add claim (e.g. familyName, givenName, email)"
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
              onKeyDown={(e) => onEnter(e, addClaim)}
            />
            <Button variant="warning" type="button" onClick={addClaim}>
              Add
            </Button>
          </InputGroup>

          {claimNames.length === 0 ? (
            <Alert variant="secondary" className="mb-0">
              No claims configured for <code>{sectionKey}</code>.
            </Alert>
          ) : (
            <Accordion alwaysOpen>
              {claimNames.map((name) => (
                <ClaimAccordion
                  key={name}
                  name={name}
                  value={section[name]}
                  onRemove={() => onChange((s) => delete s[name])}
                  onUpdate={(fn) =>
                    onChange((s) => {
                      s[name] = fn(s[name]);
                    })
                  }
                />
              ))}
            </Accordion>
          )}
        </Accordion.Body>
      </Accordion.Item>
    </Accordion>
  );
}

/* =========================
 SINGLE CLAIM (Claim = Accordion Item)
 - New switch: "request as null"
 - When enabled => value becomes null and attribute UI is hidden
 ========================= */

function ClaimAccordion({ name, value, onRemove, onUpdate }) {
  const isNull = isClaimNull(value);
  const objForRead = isNull ? {} : ensureClaimObject(value);

  const valuesArr = Array.isArray(objForRead.values) ? objForRead.values : [];
  const singleValue = typeof objForRead.value === "string" ? objForRead.value : "";

  const customEntries = isPlainObject(objForRead)
    ? Object.entries(objForRead).filter(([k]) => !["essential", "values", "value"].includes(k))
    : [];

  return (
    <Accordion.Item eventKey={name}>
      <Accordion.Header>
        <span className="d-inline-flex align-items-center gap-2 flex-wrap">
          <strong>{name}</strong>
          {isNull && <Badge bg="secondary">null</Badge>}
          {objForRead.essential === true && <Badge bg="warning" text="dark">essential</Badge>}
          {valuesArr.length > 0 && <Badge bg="info">values:{valuesArr.length}</Badge>}
          {singleValue && <Badge bg="secondary">value</Badge>}
          {customEntries.length > 0 && <Badge bg="dark">custom:{customEntries.length}</Badge>}
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
        {/* NEW SWITCH: request as null */}
        <Form.Check
          type="switch"
          id={`null-request-${name}`}
          label="request as null"
          checked={isNull}
          onChange={(e) => {
            const checked = e.target.checked;
            onUpdate((prev) => {
              if (checked) {
                // Force null, hide everything else
                return null;
              }
              // Switch off: make sure it's editable again
              // If it was null, convert to empty object.
              return prev === null ? {} : prev;
            });
          }}
        />

        {/* When null-request is ON, hide the other controls */}
        {!isNull && (
          <>
            <Form.Check
              type="switch"
              id={`essential-${name}`}
              label="essential"
              checked={objForRead.essential === true}
              onChange={(e) => {
                const checked = e.target.checked;
                onUpdate((prev) => {
                  const obj = ensureClaimObject(prev);
                  if (checked) obj.essential = true;
                  else delete obj.essential;
                  return obj;
                });
              }}
            />

            <Accordion className="mt-3">
              <Accordion.Item eventKey={`${name}-value`}>
                <Accordion.Header>value (single)</Accordion.Header>
                <Accordion.Body>
                  <Form.Control
                    value={singleValue}
                    placeholder='Single value test (e.g. "admin")'
                    onChange={(e) => {
                      const v = e.target.value;
                      onUpdate((prev) => {
                        const obj = ensureClaimObject(prev);
                        if (v && v.trim().length > 0) obj.value = v;
                        else delete obj.value;
                        return obj;
                      });
                    }}
                  />
                </Accordion.Body>
              </Accordion.Item>

              <Accordion.Item eventKey={`${name}-values`}>
                <Accordion.Header>values (multi)</Accordion.Header>
                <Accordion.Body>
                  <ValuesEditor
                    values={valuesArr}
                    onChangeValues={(nextValues) => {
                      onUpdate((prev) => {
                        const obj = ensureClaimObject(prev);
                        if (nextValues.length > 0) obj.values = nextValues;
                        else delete obj.values;
                        return obj;
                      });
                    }}
                  />
                </Accordion.Body>
              </Accordion.Item>

              <Accordion.Item eventKey={`${name}-custom`}>
                <Accordion.Header>
                  Custom attributes{" "}
                  <span className="ms-2">
                    {customEntries.length > 0 ? (
                      <Badge bg="dark">count:{customEntries.length}</Badge>
                    ) : (
                      <Badge bg="secondary">none</Badge>
                    )}
                  </span>
                </Accordion.Header>
                <Accordion.Body>
                  <CustomAttributesEditor
                    entries={customEntries}
                    onAdd={(key) => {
                      onUpdate((prev) => {
                        const obj = ensureClaimObject(prev);
                        const k = (key ?? "").trim();
                        if (!k) return prev;
                        if (obj[k] === undefined) obj[k] = "";
                        return obj;
                      });
                    }}
                    onRemove={(key) => {
                      onUpdate((prev) => {
                        const obj = ensureClaimObject(prev);
                        delete obj[key];
                        return obj;
                      });
                    }}
                    onUpdate={(key, rawValue) => {
                      onUpdate((prev) => {
                        const obj = ensureClaimObject(prev);
                        const parsed = safeJsonParse(rawValue);
                        obj[key] = parsed !== null ? parsed : rawValue;
                        return obj;
                      });
                    }}
                  />
                </Accordion.Body>
              </Accordion.Item>
            </Accordion>
          </>
        )}
      </Accordion.Body>
    </Accordion.Item>
  );
}

/* =========================
 Values editor
 ========================= */

function ValuesEditor({ values, onChangeValues }) {
  const [draft, setDraft] = useState("");

  const addValue = useCallback(() => {
    const v = draft.trim();
    if (!v) return;
    if (!values.includes(v)) onChangeValues([...values, v]);
    setDraft("");
  }, [draft, values, onChangeValues]);

  return (
    <>
      <InputGroup className="mb-2">
        <Form.Control
          value={draft}
          placeholder='Add a value and press "Add"'
          onChange={(e) => setDraft(e.target.value)}
          onKeyDown={(e) => onEnter(e, addValue)}
        />
        <Button variant="outline-warning" type="button" onClick={addValue}>
          Add
        </Button>
        <Button
          variant="outline-secondary"
          type="button"
          disabled={values.length === 0}
          onClick={() => onChangeValues([])}
        >
          Clear
        </Button>
      </InputGroup>

      {values.length > 0 && (
        <div className="d-flex flex-wrap gap-2">
          {values.map((v) => (
            <Badge bg="light" text="dark" key={v} style={{ border: "1px solid #ddd" }}>
              {v}{" "}
              <Button
                variant="link"
                size="sm"
                className="p-0 ms-2"
                style={{ textDecoration: "none" }}
                type="button"
                onClick={() => onChangeValues(values.filter((x) => x !== v))}
                aria-label={`Remove ${v}`}
              >
                ✕
              </Button>
            </Badge>
          ))}
        </div>
      )}
    </>
  );
}

/* =========================
 Custom attributes editor
 ========================= */

function CustomAttributesEditor({ entries, onAdd, onRemove, onUpdate }) {
  const [newKey, setNewKey] = useState("");

  const addAttr = useCallback(() => {
    const k = newKey.trim();
    if (!k) return;
    onAdd(k);
    setNewKey("");
  }, [newKey, onAdd]);

  return (
    <>
      <InputGroup className="mb-2">
        <Form.Control
          value={newKey}
          placeholder="custom key (e.g. purpose, reason, debug)"
          onChange={(e) => setNewKey(e.target.value)}
          onKeyDown={(e) => onEnter(e, addAttr)}
        />
        <Button variant="outline-warning" type="button" onClick={addAttr}>
          Add
        </Button>
      </InputGroup>

      {entries.length === 0 ? (
        <div className="text-muted" style={{ fontSize: 13 }}>
          No custom attributes.
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
                  onBlur={(e) => onUpdate(k, e.target.value)}
                  onKeyDown={(e) =>
                    onEnter(e, () => {
                      onUpdate(k, e.target.value);
                      e.currentTarget.blur();
                    })
                  }
                />
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
