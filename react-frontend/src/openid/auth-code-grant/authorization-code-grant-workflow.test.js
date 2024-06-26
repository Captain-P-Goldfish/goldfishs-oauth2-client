import React from 'react';
import {act, render, waitFor} from '@testing-library/react';
import {unmountComponentAtNode} from "react-dom";
import {AccessTokenResponse} from "./authorization-code-grant-workflow";
import Assertions from "../../setupTests";

let container = null;

/* ********************************************************************************************************* */

beforeEach(() =>
{
  window.MAX_RESULTS = 10;
  // setup a DOM element as a render target
  container = document.createElement("div");
  document.body.appendChild(container);
});

/* ********************************************************************************************************* */

afterEach(() =>
{
  // cleanup on exiting
  unmountComponentAtNode(container);
  container.remove();
  container = null;
});


test("Render AccessTokenResponse With Bearer Token", async () =>
{

  // let bearerToken = "{\"access_token\": \"abc\", \"token_type\": \"Bearer\", \"expires_in\": 3600, "
  //                   + "\"refresh_token\": \"cba\"}";
  // let contentType = "application/json;encoding=utf-8";
  // act(() =>
  // {
  //     render(<AccessTokenResponse contentType={contentType}
  //                                 tokenResponse={bearerToken} />, container);
  // });
  //
  // await waitFor(() =>
  // {
  //     expect(new Assertions("#access-token-response-container").isPresent().isVisible());
  // })

  let authUrl = new URL(
    "http://localhost:8081/auth/realms/goldfish/protocol/openid-connect/auth?client_id=goldfish-rest-client&response_type=code&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fauthcode&state=bab36cc5-73a7-4db7-9a01-dca7a6981256");
  const params = Object.fromEntries(authUrl.searchParams);
})

test("Render AccessTokenResponse With Plain Token", async () =>
{
  let plainToken = Math.random().toString(36).substring(2, 15);
  let contentType = "text/plain";
  act(() =>
  {
    render(<AccessTokenResponse contentType={contentType}
                                tokenResponse={plainToken}/>, container);
  });

  await waitFor(() =>
  {
    expect(new Assertions("#access-token-response-container").isPresent().isVisible());
  })


})

test("Render AccessTokenResponse With Custom Token", async () =>
{

  let customToken = "<token>" + Math.random().toString(36).substring(2, 15) + "</token>";
  let contentType = "application/xml";
  act(() =>
  {
    render(<AccessTokenResponse contentType={contentType}
                                tokenResponse={customToken}/>, container);
  });

  await waitFor(() =>
  {
    expect(new Assertions("#access-token-response-container").isPresent().isVisible());
  })


})

