import {useEffect, useRef, useState} from "react";

export function toBase64(file)
{
  return new Promise((resolve, reject) =>
  {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () =>
    {
      let encoded = reader.result.toString().replace(/^data:(.*,)?/, '');
      if ((encoded.length % 4) > 0)
      {
        encoded += '='.repeat(4 - (encoded.length % 4));
      }
      resolve(encoded);
    };
    reader.onerror = error => reject(error);
  });
}

export function downloadBase64Data(base64Data, filename, filetype)
{
  let decoded = window.atob(base64Data);
  let len = decoded.length;
  let bytes = new Uint8Array(len);
  for (let i = 0; i < len; i++)
  {
    bytes[i] = decoded.charCodeAt(i);
  }
  let file = new Blob([bytes.buffer], {type: filetype});
  let downloadAnchor = document.createElement("a");
  let url = URL.createObjectURL(file);
  downloadAnchor.href = url;
  downloadAnchor.download = filename;
  downloadAnchor.click();
  setTimeout(function ()
  {
    window.URL.revokeObjectURL(url);
  }, 0);
}

export function parseJws(token)
{
  if (typeof token !== 'string')
  {
    return null;
  }
  let jws = token.split('.');
  if (jws.length !== 3)
  {
    return null;
  }

  function decode(content)
  {
    let base64 = content.replace(/-/g, '+').replace(/_/g, '/');
    return decodeURIComponent(Buffer.from(base64, "base64").toString().split('').map(function (c)
    {
      return '%' + ('00' + c.charCodeAt(
        0).toString(16)).slice(-2);
    }).join(''));
  }

  let header = decode(jws[0]);
  let payload = decode(jws[1]);
  let signature = jws[2];
  return {
    header: header,
    payload: payload,
    signature: signature
  };
}

export class Optional
{
  constructor(value)
  {
    this.value = value;
  }

  get()
  {
    return this.value;
  }

  isPresent()
  {
    return this.value !== undefined && this.value !== null;
  }

  ifPresent(handler)
  {
    if (this.isPresent())
    {
      handler(this.value);
    }
    return this;
  }

  ifNotPresent(handler)
  {
    if (!this.isPresent())
    {
      handler();
    }
    return this;
  }

  isEmpty()
  {
    return this.value === undefined || this.value === null;
  }

  filter(handler)
  {
    if (this.isPresent() && !handler(this.value))
    {
      this.value = null;
    }
    return this;
  }

  map(handler)
  {
    if (this.isPresent())
    {
      this.value = handler(this.value);
    }
    return this;
  }

  do(handler)
  {
    if (this.isPresent())
    {
      handler(this.value);
    }
    return this;
  }

  orElse(defaultValue)
  {
    if (this.isPresent())
    {
      return this.value;
    } else
    {
      return defaultValue;
    }
  }
}

export function prettyPrintXml(sourceXml)
{
  var xmlDoc = new DOMParser().parseFromString(sourceXml, 'application/xml');
  var xsltDoc = new DOMParser().parseFromString([
    // describes how we want to modify the XML - indent everything
    '<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">',
    '  <xsl:strip-space elements="*"/>',
    '  <xsl:template match="para[content-style][not(text())]">', // change to just text() to strip space in text
                                                                 // nodes
    '    <xsl:value-of select="normalize-space(.)"/>',
    '  </xsl:template>',
    '  <xsl:template match="node()|@*">',
    '    <xsl:copy><xsl:apply-templates select="node()|@*"/></xsl:copy>',
    '  </xsl:template>',
    '  <xsl:output indent="yes"/>',
    '</xsl:stylesheet>',
  ].join('\n'), 'application/xml');

  var xsltProcessor = new XSLTProcessor();
  xsltProcessor.importStylesheet(xsltDoc);
  var resultDoc = xsltProcessor.transformToDocument(xmlDoc);
  var resultXml = new XMLSerializer().serializeToString(resultDoc);
  return resultXml;
}

export function useRenderCount()
{
  const renderCount = useRef(1);

  useEffect(() =>
  {
    renderCount.current = renderCount.current + 1;
  });

  return [renderCount.current];
}

export function useScimErrorResponse()
{
  const [errorResponse, setErrorResponse] = useState();

  useEffect(() =>
  {
    if (new Optional(errorResponse).isPresent())
    {

      alert("Server returned an error:\n" + JSON.stringify(errorResponse, null, 2));
    }
  }, [errorResponse]);

  return [setErrorResponse];
}
