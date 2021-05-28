import React from 'react';
import {act, render, waitFor} from '@testing-library/react';
import Assertions, {mockFetch} from "../setupTests"
import {unmountComponentAtNode} from "react-dom";
import OpenidProvider from "./openid-provider";
import {Optional} from "../services/utils";

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

/* ********************************************************************************************************* */

function getFakeProviderInfos()
{
    return {
        "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
        "totalResults": 3,
        "itemsPerPage": 3,
        "startIndex": 1,
        "Resources": [{
            "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:OpenIdProvider"],
            "id": "1",
            "name": "keycloak",
            "discoveryEndpoint": "http://localhost:8080",
            "meta": {
                "resourceType": "OpenIdProvider",
                "created": "2021-05-22T21:07:20.000Z",
                "lastModified": "2021-05-26T15:08:08.000Z",
                "location": "http://localhost:8080/scim/v2/OpenIdProvider/1004"
            }
        }, {
            "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:OpenIdProvider"],
            "id": "2",
            "name": "facebook",
            "discoveryEndpoint": "http://localhost:8080",
            "resourceEndpoints": ["http://localhost:8080/auth/realms/master/scim/v2/Groups",
                                  "http://localhost:8080/auth/realms/master/scim/v2/Users"],
            "meta": {
                "resourceType": "OpenIdProvider",
                "created": "2021-05-22T21:07:20.000Z",
                "lastModified": "2021-05-26T15:08:08.000Z",
                "location": "http://localhost:8080/scim/v2/OpenIdProvider/1004"
            }
        }, {
            "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:OpenIdProvider"],
            "id": "3",
            "name": "google",
            "authorizationEndpoint": "http://localhost:8080",
            "tokenEndpoint": "http://localhost:8080",
            "resourceEndpoints": ["http://localhost:8080/auth/realms/master/scim/v2/Groups"],
            "signatureVerificationKey": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwP5+EhHyk1HTZ+Zt9nOHSyFeX"
                                        + "+Ks4CtNvqmEshtbrXMAIIlJi4Sm6pm9GHlqDPtSbKM9PPvJCd8mD/4oIjI/a1wKyfpZ"
                                        + "0Nq5MOuRK5YGeuVrQTY6EdOt8U2feJwr582nKaP4zAYzoWeSO2CcY5M1exu0L4DmiPF"
                                        + "l5LBCAKYGxuoj7kycH8tvtmrgt60V27HKmIpYBjALdDxJg21ybjHvYtoHJ34ngQSMBN"
                                        + "qqkmg/zdkdK5lIpQxRYsZyM2zCYj1/Aq+ZALtOsjYI3qf96l2yAT7hW11Ew7Iqh4bfm"
                                        + "RB6nimf7mtG3xMivvviz62MaTUtEz+5Ud8EKD4roCrEjdu/QQIDAQAB",
            "meta": {
                "resourceType": "OpenIdProvider",
                "created": "2021-05-22T21:07:20.000Z",
                "lastModified": "2021-05-26T15:08:08.000Z",
                "location": "http://localhost:8080/scim/v2/OpenIdProvider/1004"
            }
        }]
    };
}

/* ********************************************************************************************************* */

function loadPageWithoutEntries()
{
    mockFetch(200, {
        Resources: []
    });

    act(() =>
    {
        render(<OpenidProvider />, container);
    });
    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/OpenIdProvider", {method: "GET"})
    global.fetch.mockRestore();
}

/* ********************************************************************************************************* */

test("verify certificate data is displayed", async () =>
{
    const fakeProviderInfos = getFakeProviderInfos();

    mockFetch(200, fakeProviderInfos);

    act(() =>
    {
        render(<OpenidProvider />, container);
    });
    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/OpenIdProvider?startIndex=1&count=10&sortBy=name", {method: "GET"})
    global.fetch.mockRestore();

    await waitFor(() =>
    {
        expect(new Assertions(".card-deck").isPresent().isVisible().element.children).toHaveLength(3);
    })

    for (let i = 0; i < fakeProviderInfos.Resources.length; i++)
    {
        let providerInfo = fakeProviderInfos.Resources[i];
        new Assertions("#provider-card-header-" + providerInfo.id)
            .isPresent().isVisible().assertEquals(providerInfo.name);
        new Assertions("#reset-update-icon-" + providerInfo.id).isNotPresent();
        new Assertions("#save-icon-" + providerInfo.id).isNotPresent();
        new Assertions("#update-icon-" + providerInfo.id).isPresent().isVisible();
        new Assertions("#delete-icon-" + providerInfo.id).isPresent().isVisible();

        new Assertions("#card-cell-" + providerInfo.id + "-discoveryEndpoint")
            .isPresent().isVisible().assertEquals(providerInfo.discoveryEndpoint);
        new Assertions("#card-cell-" + providerInfo.id + "-authorizationEndpoint")
            .isPresent().isVisible().assertEquals(providerInfo.authorizationEndpoint);
        new Assertions("#card-cell-" + providerInfo.id + "-tokenEndpoint")
            .isPresent().isVisible().assertEquals(providerInfo.tokenEndpoint);
        new Assertions("#card-cell-" + providerInfo.id + "-signatureVerificationKey")
            .isPresent().isVisible().assertEquals(providerInfo.signatureVerificationKey);


        let resourceEndpointElement = new Assertions("#card-cell-" + providerInfo.id + "-resourceEndpoints").element;
        new Optional(providerInfo.resourceEndpoints).ifPresent(endpointArray =>
        {
            expect(resourceEndpointElement.children).toHaveLength(1);
            let unorderdList = resourceEndpointElement.children[0];
            expect(unorderdList.children).toHaveLength(endpointArray.length);
            for (let j = 0; j < endpointArray.length; j++)
            {
                let endpoint = endpointArray[j];
                let uiEndpoint = unorderdList.children[j].textContent;
                expect(endpoint).toEqual(uiEndpoint);
            }
        }).ifNotPresent(() =>
        {
            expect(resourceEndpointElement.children).toHaveLength(0);
        })
    }
});
