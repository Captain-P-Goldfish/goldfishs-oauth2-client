import React from 'react';
import {act, render, waitFor} from '@testing-library/react';
import Assertions, {mockFetch} from "../setupTests"
import {unmountComponentAtNode} from "react-dom";
import OpenidProvider from "./openid-provider";
import {Optional, toBase64} from "../services/utils";

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
    expect(global.fetch).toBeCalledWith("/scim/v2/OpenIdProvider?startIndex=1&count=10&sortBy=name", {method: "GET"})
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

/* ********************************************************************************************************* */

test("create new openid provider", async () =>
{
    loadPageWithoutEntries();

    mockFetch(200, {Resources: []});

    act(() =>
    {
        render(<OpenidProvider />, container);
    });
    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/OpenIdProvider?startIndex=1&count=10&sortBy=name", {method: "GET"})
    global.fetch.mockRestore();

    expect(new Assertions(".card-deck").isPresent().isVisible().element.children).toHaveLength(0);

    await new Assertions("p.add-new-resource").assertEquals("Add new Provider").clickElement(() =>
    {
        new Assertions("#reset-update-icon-undefined").isNotPresent();
        new Assertions("#update-icon-undefined").isNotPresent();
        new Assertions("#save-icon-undefined").isPresent().isVisible();
        new Assertions("#delete-icon-undefined").isPresent().isVisible();
    });

    let nameField = new Assertions("input#name-undefined").isPresent().isVisible();
    let discoveryEndpointField = new Assertions("input#discoveryEndpoint-undefined").isPresent().isVisible();
    let authEndpointField = new Assertions("input#authorizationEndpoint-undefined").isPresent().isVisible();
    let tokenEndpointField = new Assertions("input#tokenEndpoint-undefined").isPresent().isVisible();
    let addResourceEndpointButton = new Assertions("#card-cell-undefined-resourceEndpoints button.add-item")
        .isPresent().isVisible().assertEquals("Add new");
    let signatureVerificationKeyField = new Assertions("input#signatureVerificationKey-undefined").isPresent()
                                                                                                  .isVisible();
    new Assertions("#card-cell-undefined-signatureVerificationKey svg.remove-index").isPresent().isVisible();

    const name = "goldfish";
    const discoveryEndpoint = "www.captaingoldfish.de/.well-known/openid-discovery";
    const authEndpoint = "www.captaingoldfish.de/authcode";
    const tokenEndpoint = "www.captaingoldfish.de/token";
    const resourceEndpoints = ["www.captaingoldfish.de/resource-1", "www.captaingoldfish.de/resource-2",
                               "www.captaingoldfish.de/resource-3"];
    const filename = "pub.cer";
    const signatureVerificationKey = new File([new Blob(['hello-world'], {type: 'text/plain'})], filename);
    const b64SignatureVerificationKey = await toBase64(signatureVerificationKey);

    await nameField.fireChangeEvent(name);
    await discoveryEndpointField.fireChangeEvent(discoveryEndpoint);
    await authEndpointField.fireChangeEvent(authEndpoint);
    await tokenEndpointField.fireChangeEvent(tokenEndpoint);
    await signatureVerificationKeyField.fireChangeEvent(signatureVerificationKey);
    for (let i = 0; i < resourceEndpoints.length; i++)
    {
        let resourceEndpoint = resourceEndpoints[i];
        let resourceEndpointField;
        await addResourceEndpointButton.clickElement(() =>
        {
            resourceEndpointField = new Assertions("input#resourceEndpoints-undefined-" + i).isPresent().isVisible();
        })
        await resourceEndpointField.fireChangeEvent(resourceEndpoint)
    }

    let request = {
        name: name,
        discoveryEndpoint: discoveryEndpoint,
        authorizationEndpoint: authEndpoint,
        tokenEndpoint: tokenEndpoint,
        resourceEndpoints: resourceEndpoints,
        signatureVerificationKey: b64SignatureVerificationKey
    }

    let response = {
        "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:OpenIdProvider"],
        "id": 5008,
        "name": name,
        "authorizationEndpoint": authEndpoint,
        "tokenEndpoint": tokenEndpoint,
        "resourceEndpoints": resourceEndpoints,
        "signatureVerificationKey": b64SignatureVerificationKey,
        "meta": {
            "resourceType": "OpenIdProvider",
            "created": "2021-05-22T21:07:20.000Z",
            "lastModified": "2021-05-26T15:08:08.000Z",
            "location": "http://localhost:8080/scim/v2/OpenIdProvider/1004"
        }
    }

    mockFetch(201, response);

    await new Assertions("#save-icon-undefined").isPresent().isVisible().clickElement(() =>
    {
        new Assertions("#provider-card-header-" + response.id).isPresent().isVisible();
    });

    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/OpenIdProvider",
        {
            method: "POST",
            headers: {'Content-Type': 'application/scim+json'},
            body: JSON.stringify(request)
        })
    global.fetch.mockRestore();
});

