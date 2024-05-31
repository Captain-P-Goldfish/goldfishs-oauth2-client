import {Buffer} from "buffer";
import * as lodash from "lodash";

export function uuidv4()
{
  return "10000000-1000-4000-8000-100000000000".replace(/[018]/g, c =>
    (+c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> +c / 4).toString(16)
  );
}

export function decodeJws(jws)
{
  if (!jws || !(typeof jws === 'string' || jws instanceof String))
  {
    return jws;
  }
  let jwsParts = jws.split(".");
  if (jwsParts.length !== 3)
  {
    return jws;
  }

  let base64Header = jwsParts[0];
  let base64Body = jwsParts[1];

  let parsedHeader = decodeBase64(base64Header);
  if (!isJson(parsedHeader))
  {
    return jws;
  }
  parsedHeader = prettyPrintJson(parsedHeader);
  let parsedBody = decodeBase64(base64Body);
  parsedBody = prettyPrintJson(parsedBody);

  let jwtDetails = {};
  jwtDetails.part1 = jwsParts[0];
  jwtDetails.part2 = jwsParts[1];
  jwtDetails.part3 = jwsParts[2];
  jwtDetails.header = prettyPrintJson(parsedHeader);
  jwtDetails.body = prettyPrintJson(parsedBody);
  return jwtDetails;
}

export function isDecodedJws(decodedJws)
{
  return isObject(decodedJws)
    && lodash.has(decodedJws, 'header')
    && lodash.has(decodedJws, 'body')
    && lodash.has(decodedJws, 'part1')
    && lodash.has(decodedJws, 'part2')
    && lodash.has(decodedJws, 'part3')
}

export function decodeBase64(value)
{
  return decodeURIComponent(Buffer.from(value, "base64").toString());
}

export function isJson(str)
{
  try
  {
    JSON.parse(str);
    return true;
  } catch (e)
  {
    return false;
  }
}

export function prettyPrintJson(value)
{
  try
  {
    let json = JSON.parse(value);
    return JSON.stringify(json, null, 2);
  } catch (ex)
  {
    return value;
  }
}

export function isObject(variable)
{
  return typeof variable === 'object' && !Array.isArray(variable) && variable !== null

}
