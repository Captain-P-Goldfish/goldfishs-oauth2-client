import {toHeaderString} from "./header-utils";

export function toHttpResponseForHistory(requestResponse)
{
  return {
    id: requestResponse.responseId,
    originalRequest: requestResponse.httpMethod + " " + requestResponse.url + "\n" + toHeaderString(
      requestResponse.requestHeaders) + "\n\n" + requestResponse.requestBody,
    status: requestResponse.responseStatus,
    responseBody: requestResponse.responseBody,
    responseHeaders: toHeaderString(requestResponse.responseHeaders),
    created: requestResponse.meta.created,
    lastModified: requestResponse.meta.lastModified
  };
}
