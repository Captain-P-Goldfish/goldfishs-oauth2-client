import React, {useMemo, useState} from "react";
import {Badge, Button, ButtonGroup, Card, Form, ListGroup} from "react-bootstrap";
import {AiOutlineDownload, AiOutlineSafetyCertificate} from "react-icons/ai";

/**
 * X5C Certificate Download Panel
 *
 * - Offers downloads as DER .cer (per-certificate) OR PEM (single chain file).
 * - Uses the previously discussed x5c conversion methods (base64 -> DER bytes, base64 -> PEM blocks).
 * - Browser-only (no backend), no new CSS required.
 *
 * Props:
 * - x5c: string[] | undefined
 * - filenamePrefix?: string (default: "x5c")
 */
export function X5cCertificateDownloadPanel({x5c, filenamePrefix = "x5c"})
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

  return (
    <Card className="shadow-sm">
      <Card.Header className="d-flex justify-content-between align-items-center">
        <div className="d-flex align-items-center gap-2">
          <AiOutlineSafetyCertificate />
          <strong>X.509 certificate chain (x5c)</strong>
          <Badge bg={canDownload ? "secondary" : "warning"}>{chain.length}</Badge>
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
            disabled={!canDownload}
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

      {!canDownload ? (
        <Card.Body className="text-muted">
          No <code>x5c</code> certificate chain present.
        </Card.Body>
      ) : (
        <>
          <Card.Body className="pb-2">
            <Form.Text className="text-muted">
              {format === "cer"
                ? "Downloads each certificate as DER-encoded .cer (one file per chain entry)."
                : "Downloads the full chain as a single PEM file (multiple CERTIFICATE blocks)."}
            </Form.Text>
          </Card.Body>

          <ListGroup variant="flush">
            {items.map(({index, role, b64}) => (
              <ListGroup.Item
                key={index}
                className="d-flex justify-content-between align-items-center"
                style={{gap: 12}}
              >
                <div>
                  <div className="fw-bold">
                    {role} certificate <Badge bg="info" className="ms-2">#{index + 1}</Badge>
                  </div>
                  <div className="text-muted" style={{fontSize: 12}}>
                    {format === "cer"
                      ? "DER (.cer) – direct import in many tools"
                      : "PEM (.pem) – common for OpenSSL/CLI"}
                  </div>
                </div>

                <div className="d-flex gap-2">
                  {format === "cer" ? (
                    <Button
                      size="sm"
                      variant="outline-light"
                      onClick={() =>
                        downloadSingleAsCer(
                          b64,
                          `${filenamePrefix}-${String(index + 1).padStart(2, "0")}-${role.toLowerCase()}.cer`
                        )
                      }
                      title="Download as .cer"
                    >
                      <AiOutlineDownload />
                    </Button>
                  ) : (
                    <Button
                      size="sm"
                      variant="outline-light"
                      onClick={() =>
                        downloadSingleAsPem(
                          b64,
                          `${filenamePrefix}-${String(index + 1).padStart(2, "0")}-${role.toLowerCase()}.pem`
                        )
                      }
                      title="Download as .pem"
                    >
                      <AiOutlineDownload />
                    </Button>
                  )}
                </div>
              </ListGroup.Item>
            ))}
          </ListGroup>
        </>
      )}
    </Card>
  );
}

/* ------------------------- helpers: CER (DER) ------------------------- */

function base64ToUint8Array(b64)
{
  // x5c is typically base64 (not base64url), but we normalize anyway:
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
  const blob = new Blob([bytes], {type: "application/pkix-cert"}); // DER cert
  downloadBlob(blob, filename);
}

function downloadAllAsCer(x5c, prefix)
{
  (x5c || []).forEach((b64, i) => {
    downloadSingleAsCer(b64, `${prefix}-${String(i + 1).padStart(2, "0")}.cer`);
  });
}

/* ------------------------------ helpers: PEM ------------------------------ */

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

export function X5cPathHeader({path, index, kid, label})
{
  const safePath = Array.isArray(path) ? path : null;
  const safeIndex = Number.isFinite(index) ? index : null;

  const pathText = safePath ? safePath.join(" › ") : (label || "(unknown path)");
  const indexText = safeIndex != null ? `#${safeIndex}` : null;

  return (
    <div className="d-flex flex-wrap align-items-center gap-2">
      <code className="text-info">{pathText}</code>

      {indexText && <Badge bg="secondary">{indexText}</Badge>}

      {kid && (
        <span className="d-flex align-items-center gap-1">
    <span className="text-muted">kid:</span>

    <code className="text-break"
          style={{
            maxWidth: 360,
            whiteSpace: "normal",
            wordBreak: "break-all",
            cursor: "text",
          }}
          title={kid}>
      {kid}
    </code>

    <Button size="sm" variant="outline-light" title="Copy kid"
            onClick={(e) => {
              e.stopPropagation(); // IMPORTANT: don't toggle accordion
              navigator.clipboard.writeText(kid);
            }}>
      Copy kid
    </Button>
  </span>
      )}

    </div>
  );
}

