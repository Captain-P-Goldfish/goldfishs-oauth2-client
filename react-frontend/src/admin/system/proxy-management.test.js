import React from 'react';
import {act, render, waitFor} from '@testing-library/react';
import Assertions, {mockFetch} from "../setupTests"
import {unmountComponentAtNode} from "react-dom";
import ProxyConfigForm from "./proxy-management";
import {Optional} from "../services/utils";

let container = null;

/* ********************************************************************************************************* */

beforeEach(() =>
{
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

/* *********
 * ************************************************************************************************ */

function loadPageWithoutEntries()
{
    mockFetch(200, {
        Resources: []
    });

    act(() =>
    {
        render(<ProxyConfigForm />, container);
    });
    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/Proxy", {method: "GET"})
    global.fetch.mockRestore();
}

/* ********************************************************************************************************* */

function getFakeProxies()
{
    return {
        "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
        "totalResults": 1,
        "itemsPerPage": 1,
        "startIndex": 1,
        "Resources": [{
            "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:Proxy"],
            "id": "1",
            "hostname": "localhost",
            "port": 8888,
            "meta": {
                "resourceType": "Proxy",
                "created": "2021-05-19T19:49:07.000Z",
                "lastModified": "2021-05-20T09:23:26.000Z",
                "location": "http://localhost:8080/scim/v2/Proxy/10"
            }
        }, {
            "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:Proxy"],
            "id": "2",
            "hostname": "happy",
            "port": 1234,
            "username": "goldfish",
            "password": "123456",
            "meta": {
                "resourceType": "Proxy",
                "created": "2021-05-19T19:49:07.000Z",
                "lastModified": "2021-05-20T09:23:26.000Z",
                "location": "http://localhost:8080/scim/v2/Proxy/10"
            }
        }, {
            "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:Proxy"],
            "id": "3",
            "hostname": "blubb",
            "port": 9876,
            "username": "goldfish",
            "password": "123456",
            "meta": {
                "resourceType": "Proxy",
                "created": "2021-05-19T19:49:07.000Z",
                "lastModified": "2021-05-20T09:23:26.000Z",
                "location": "http://localhost:8080/scim/v2/Proxy/10"
            }
        }]
    };
}

/* ********************************************************************************************************* */

test("verify proxies are displayed", async () =>
{
    const fakeProxies = getFakeProxies();

    mockFetch(200, fakeProxies);

    act(() =>
    {
        render(<ProxyConfigForm />, container);
    });
    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/Proxy", {method: "GET"})
    global.fetch.mockRestore();

    await waitFor(() =>
    {
        new Assertions("#proxy-card-1").isPresent().isVisible();
        new Assertions("#proxy-card-2").isPresent().isVisible();
        new Assertions("#proxy-card-3").isPresent().isVisible();
    });

    for (let resource of fakeProxies.Resources)
    {
        new Assertions("#proxy-card-" + resource.id + "-hostname").assertEquals(resource.hostname)
        new Assertions("#proxy-card-" + resource.id + "-port").assertEquals(resource.port.toString())
        new Assertions("#proxy-card-" + resource.id + "-username").assertEquals(
            new Optional(resource.username).orElse(""))
        new Assertions("#proxy-card-" + resource.id + "-password").assertEquals(
            new Optional(resource.password).orElse(""))
    }
});

/* ********************************************************************************************************* */

test("create proxy", async () =>
{

    loadPageWithoutEntries();

    await new Assertions("#hostname").fireChangeEvent("localhost");
    await new Assertions("#port").fireChangeEvent(3344);
    await new Assertions("#username").fireChangeEvent("goldfish");
    await new Assertions("#password").fireChangeEvent("123456");

    mockFetch(200, {
        "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:Proxy"],
        "id": "1",
        "hostname": "localhost",
        "port": 3344,
        "username": "goldlfish",
        "password": "123456",
        "meta": {
            "resourceType": "Proxy",
            "created": "2021-05-19T19:49:07.000Z",
            "lastModified": "2021-05-20T09:23:26.000Z",
            "location": "http://localhost:8080/scim/v2/Proxy/10"
        }
    });

    await new Assertions("#submitButton").assertEquals("save").clickElement(() =>
    {
        new Assertions("#proxy-card-1").isPresent().isVisible();
    });

    let data = {
        "hostname": "localhost",
        "port": 3344,
        "username": "goldfish",
        "password": "123456"
    };
    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/Proxy",
        {
            method: "POST",
            headers: {'Content-Type': 'application/scim+json'},
            body: JSON.stringify(data)
        })
    global.fetch.mockRestore();

    await new Assertions("#hostname").assertEquals("");
    await new Assertions("#port").assertEquals("");
    await new Assertions("#username").assertEquals("");
    await new Assertions("#password").assertEquals("");
});

test("update proxy", async () =>
{
    const fakeProxies = {
        "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
        "totalResults": 1,
        "itemsPerPage": 1,
        "startIndex": 1,
        "Resources": [{
            "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:Proxy"],
            "id": "1",
            "hostname": "localhost",
            "port": 8888,
            "meta": {
                "resourceType": "Proxy",
                "created": "2021-05-19T19:49:07.000Z",
                "lastModified": "2021-05-20T09:23:26.000Z",
                "location": "http://localhost:8080/scim/v2/Proxy/10"
            }
        }]
    }

    mockFetch(200, fakeProxies);

    act(() =>
    {
        render(<ProxyConfigForm />, container);
    });
    global.fetch.mockRestore();

    await waitFor(() =>
    {
        new Assertions("#proxy-card-1").isPresent().isVisible();
    });

    let resource = fakeProxies.Resources[0];

    // click update icon
    await new Assertions("#update-icon-" + resource.id).isPresent().isVisible().clickElement(() =>
    {
        new Assertions("#edit-mode-activated-alert").assertEquals(
            "Updating proxy with id '" + resource.id + "'");
        new Assertions("#hostname").hasValueSelected(resource.hostname);
        new Assertions("#port").hasValueSelected(resource.port.toString());
        new Assertions("#username").hasValueSelected(new Optional(resource.username).orElse(""));
        new Assertions("#password").hasValueSelected(new Optional(resource.password).orElse(""));

        new Assertions("#cancel").isPresent().isVisible().assertEquals("cancel");
        new Assertions("#submitButton").isPresent().isVisible().assertEquals("update");
    })

    // click cancel button
    await new Assertions("#cancel").isPresent().isVisible().clickElement(() =>
    {
        new Assertions("#edit-mode-activated-alert").isNotPresent();
        new Assertions("#hostname").assertEquals("");
        new Assertions("#port").assertEquals("");
        new Assertions("#username").assertEquals("");
        new Assertions("#password").assertEquals("");

        new Assertions("#cancel").isNotPresent();
        new Assertions("#submitButton").isPresent().isVisible().assertEquals("save");
    });

    // click update icon again
    await new Assertions("#update-icon-" + resource.id).isPresent().isVisible().clickElement(() =>
    {
        new Assertions("#edit-mode-activated-alert").assertEquals(
            "Updating proxy with id '" + resource.id + "'");
    })

    // modify values
    let newProxy = {
        hostname: "new-host-name",
        port: 6666,
        username: "new-username",
        password: "new-password"
    }
    await new Assertions("#hostname").fireChangeEvent(newProxy.hostname);
    await new Assertions("#port").fireChangeEvent(newProxy.port);
    await new Assertions("#username").fireChangeEvent(newProxy.username);
    await new Assertions("#password").fireChangeEvent(newProxy.password);

    new Assertions("#proxy-card-" + resource.id + "-hostname").assertEquals(newProxy.hostname)
    new Assertions("#proxy-card-" + resource.id + "-port").assertEquals(newProxy.port.toString())
    new Assertions("#proxy-card-" + resource.id + "-username").assertEquals(newProxy.username)
    new Assertions("#proxy-card-" + resource.id + "-password").assertEquals(newProxy.password)

    // prepare update response
    mockFetch(200, {
        "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:Proxy"],
        "id": resource.id,
        "hostname": newProxy.hostname,
        "port": newProxy.port,
        "username": newProxy.username,
        "password": newProxy.password,
        "meta": {
            "resourceType": "Proxy",
            "created": "2021-05-19T19:49:07.000Z",
            "lastModified": "2021-05-20T09:23:26.000Z",
            "location": "http://localhost:8080/scim/v2/Proxy/10"
        }
    })

    // do update
    await new Assertions("#submitButton").isPresent().isVisible().assertEquals("update").clickElement(() =>
    {
        new Assertions("#proxyForm-alert-success").isPresent().isVisible().assertEquals(
            "Proxy was successfully saved")
        new Assertions("#edit-mode-activated-alert").isNotPresent();
        new Assertions("#proxy-card-" + resource.id + "-hostname").assertEquals(newProxy.hostname)
        new Assertions("#proxy-card-" + resource.id + "-port").assertEquals(newProxy.port.toString())
        new Assertions("#proxy-card-" + resource.id + "-username").assertEquals(newProxy.username)
        new Assertions("#proxy-card-" + resource.id + "-password").assertEquals(newProxy.password)
        new Assertions("#cancel").isNotPresent();
        new Assertions("#submitButton").isPresent().isVisible().assertEquals("save");
    });

    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/Proxy/" + resource.id,
        {
            method: "PUT",
            headers: {'Content-Type': 'application/scim+json'},
            body: JSON.stringify(newProxy)
        })
    global.fetch.mockRestore();

    // make sure that no new element is displayed in the card-deck
    let childrenOfCardDeck = new Assertions(".card-deck").isPresent().isVisible().element.childNodes;
    expect(childrenOfCardDeck).toHaveLength(1);
});

test("delete proxy", async () =>
{
    const fakeProxies = {
        "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
        "totalResults": 1,
        "itemsPerPage": 1,
        "startIndex": 1,
        "Resources": [{
            "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:Proxy"],
            "id": "1",
            "hostname": "localhost",
            "port": 8888,
            "meta": {
                "resourceType": "Proxy",
                "created": "2021-05-19T19:49:07.000Z",
                "lastModified": "2021-05-20T09:23:26.000Z",
                "location": "http://localhost:8080/scim/v2/Proxy/10"
            }
        }]
    }

    mockFetch(200, fakeProxies);

    act(() =>
    {
        render(<ProxyConfigForm />, container);
    });
    global.fetch.mockRestore();

    await waitFor(() =>
    {
        new Assertions("#proxy-card-1").isPresent().isVisible();
    });

    let resource = fakeProxies.Resources[0];

    // click delete icon
    await new Assertions("#delete-icon-" + resource.id).isPresent().isVisible().clickElement(() =>
    {
        new Assertions("#delete-dialog-" + resource.id + "-header").assertEquals(
            "Delete Proxy '" + resource.id + "'");
    })

    // click cancel button
    await new Assertions("#delete-dialog-" + resource.id + "-button-cancel").isPresent().isVisible().clickElement(() =>
    {
        new Assertions("#delete-dialog-" + resource.id).isNotPresent();
    })

    // click delete icon again
    await new Assertions("#delete-icon-" + resource.id).isPresent().isVisible().clickElement(() =>
    {
        new Assertions("#delete-dialog-" + resource.id + "-header").assertEquals(
            "Delete Proxy '" + resource.id + "'");
    })

    mockFetch(204, "");

    // click delete button
    await new Assertions("#delete-dialog-" + resource.id + "-button-accept").isPresent().isVisible().clickElement(() =>
    {
        new Assertions("#delete-dialog-" + resource.id).isNotPresent();
        new Assertions("#proxy-card-" + resource.id).isNotPresent();
    })

    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/Proxy/" + resource.id,
        {
            method: "DELETE"
        })
    global.fetch.mockRestore();

    // make sure that no elements are present anymore within the card-deck
    let childrenOfCardDeck = new Assertions(".card-deck").isPresent().isVisible().element.childNodes;
    expect(childrenOfCardDeck).toHaveLength(0);
})
