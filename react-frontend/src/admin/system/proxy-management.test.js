import React from 'react';
import {act, render, waitFor} from '@testing-library/react';
import Assertions, {mockFetch} from "../../setupTests"
import {unmountComponentAtNode} from "react-dom";
import ProxyConfigForm from "./proxy-management";
import ProxyManagement from "./proxy-management";
import {Optional} from "../../services/utils";

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

/* ********************************************************************************************************* */

function getFakeProxies()
{
    return {
        "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
        "totalResults": 3,
        "itemsPerPage": 3,
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
        render(<ProxyManagement />, container);
    });

    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/Proxy?startIndex=1&sortBy=id", {method: "GET"})
    global.fetch.mockRestore();

    await waitFor(() =>
    {
        new Assertions("#proxy-card-1").isPresent().isVisible();
        new Assertions("#proxy-card-2").isPresent().isVisible();
        new Assertions("#proxy-card-3").isPresent().isVisible();
    });

    for (let resource of fakeProxies.Resources)
    {
        new Assertions("#card-cell-" + resource.id + "-hostname").assertEquals(resource.hostname)
        new Assertions("#card-cell-" + resource.id + "-port").assertEquals(resource.port.toString())
        new Assertions("#card-cell-" + resource.id + "-username").assertEquals(
            new Optional(resource.username).orElse(""))
        new Assertions("#card-cell-" + resource.id + "-password").assertEquals(
            new Optional(resource.password).orElse(""))
    }
});

/* ********************************************************************************************************* */

test("create proxy", async () =>
{

    const fakeProxies = getFakeProxies();

    mockFetch(200, fakeProxies);

    act(() =>
    {
        render(<ProxyManagement />, container);
    });
    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/Proxy?startIndex=1&sortBy=id", {method: "GET"})
    global.fetch.mockRestore();

    await new Assertions("p.add-new-resource").assertEquals("Add new Proxy").clickElement(() =>
    {
        new Assertions("#reset-update-icon-undefined").isNotPresent();
        new Assertions("#update-icon-undefined").isNotPresent();
        new Assertions("#save-icon-undefined").isPresent().isVisible();
        new Assertions("#delete-icon-undefined").isPresent().isVisible();
    });

    const id = 4;
    const hostname = "localhost";
    const port = 3344
    const username = "goldfish";
    const password = "123456";

    await new Assertions("#hostname-undefined").fireChangeEvent(hostname);
    await new Assertions("#port-undefined").fireChangeEvent(port);
    await new Assertions("#username-undefined").fireChangeEvent(username);
    await new Assertions("#password-undefined").fireChangeEvent(password);

    mockFetch(201, {
        "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:Proxy"],
        "id": id,
        "hostname": hostname,
        "port": port,
        "username": username,
        "password": password,
        "meta": {
            "resourceType": "Proxy",
            "created": "2021-05-19T19:49:07.000Z",
            "lastModified": "2021-05-20T09:23:26.000Z",
            "location": "http://localhost:8080/scim/v2/Proxy/10"
        }
    });

    let data = {
        "hostname": hostname,
        "port": port,
        "username": username,
        "password": password
    };

    await new Assertions("#save-icon-undefined").isPresent().isVisible().clickElement(() =>
    {
        new Assertions("#proxy-card-" + id).isPresent().isVisible();
    });

    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/Proxy",
        {
            method: "POST",
            headers: {'Content-Type': 'application/scim+json'},
            body: JSON.stringify(data)
        })
    global.fetch.mockRestore();

    new Assertions("#reset-update-icon-undefined").isNotPresent();
    new Assertions("#save-icon-undefined").isNotPresent();
    new Assertions("#update-icon-undefined").isNotPresent();
    new Assertions("#delete-icon-undefined").isNotPresent();

    new Assertions("#reset-update-icon-" + id).isNotPresent();
    new Assertions("#save-icon-" + id).isNotPresent();
    new Assertions("#update-icon-" + id).isPresent().isVisible();
    new Assertions("#delete-icon-" + id).isPresent().isVisible();

    new Assertions("#save-alert-success").isPresent().isVisible()
                                         .assertEquals("Proxy with id '" + id + "' was successfully created");

    new Assertions("#proxy-card-header-" + id + " h5").isPresent().isVisible()
                                                      .assertEquals("Proxy '" + id + "'");
    new Assertions("#card-cell-" + id + "-hostname").isPresent().isVisible().assertEquals(hostname);
    new Assertions("#card-cell-" + id + "-port").isPresent().isVisible().assertEquals(port.toString());
    new Assertions("#card-cell-" + id + "-username").isPresent().isVisible().assertEquals(username);
    new Assertions("#card-cell-" + id + "-password").isPresent().isVisible().assertEquals(password);

    await waitFor(() =>
    {
        expect(new Assertions(".card-deck").isPresent().isVisible().element.children).toHaveLength(4);
    });

    console.log(document.documentElement.innerHTML)
});

/* ************************************************************************************************** */

test("update proxy", async () =>
{
    const fakeProxies = getFakeProxies();

    mockFetch(200, fakeProxies);

    act(() =>
    {
        render(<ProxyManagement />, container);
    });
    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/Proxy?startIndex=1&sortBy=id", {method: "GET"})
    global.fetch.mockRestore();
    await waitFor(() =>
    {
        expect(new Assertions(".card-deck").isPresent().isVisible().element.children).toHaveLength(3);
    });

    const id = 1;
    const hostname = "fiddler";
    const port = 6666
    const username = "mario";
    const password = "654321";

    await new Assertions("#update-icon-" + id).clickElement(() =>
    {
        new Assertions("#update-icon-" + id).isNotPresent();
        new Assertions("#reset-update-icon-" + id).isPresent().isVisible();
        new Assertions("#save-icon-" + id).isPresent().isVisible();
        new Assertions("#delete-icon-" + id).isPresent().isVisible();
    });

    await new Assertions("#hostname-" + id).fireChangeEvent(hostname);
    await new Assertions("#port-" + id).fireChangeEvent(port);
    await new Assertions("#username-" + id).fireChangeEvent(username);
    await new Assertions("#password-" + id).fireChangeEvent(password);

    mockFetch(200, {
        "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:Proxy"],
        "id": id,
        "hostname": hostname,
        "port": port,
        "username": username,
        "password": password,
        "meta": {
            "resourceType": "Proxy",
            "created": "2021-05-19T19:49:07.000Z",
            "lastModified": "2021-05-20T09:23:26.000Z",
            "location": "http://localhost:8080/scim/v2/Proxy/10"
        }
    });

    let data = {
        "hostname": hostname,
        "port": port,
        "username": username,
        "password": password
    };

    await new Assertions("#save-icon-" + id).isPresent().isVisible().clickElement(() =>
    {
        new Assertions("#proxy-card-" + id).isPresent().isVisible();
        new Assertions("#reset-update-icon-" + id).isNotPresent();
        new Assertions("#save-icon-" + id).isNotPresent();
        new Assertions("#update-icon-" + id).isPresent().isVisible();
        new Assertions("#delete-icon-" + id).isPresent().isVisible();
    });

    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/Proxy/" + id,
        {
            method: "PUT",
            headers: {'Content-Type': 'application/scim+json'},
            body: JSON.stringify(data)
        })
    global.fetch.mockRestore();


    new Assertions("#save-alert-success-" + id).isPresent().isVisible()
                                               .assertEquals("Proxy was successfully updated");

    new Assertions("#proxy-card-header-" + id + " h5").isPresent().isVisible()
                                                      .assertEquals("Proxy '" + id + "'");
    new Assertions("#card-cell-" + id + "-hostname").isPresent().isVisible().assertEquals(hostname);
    new Assertions("#card-cell-" + id + "-port").isPresent().isVisible().assertEquals(port.toString());
    new Assertions("#card-cell-" + id + "-username").isPresent().isVisible().assertEquals(username);
    new Assertions("#card-cell-" + id + "-password").isPresent().isVisible().assertEquals(password);

    expect(new Assertions(".card-deck").isPresent().isVisible().element.children).toHaveLength(3);
});

/* ************************************************************************************************** */

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
            "Delete Proxy with ID '" + resource.id + "'");
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
            "Delete Proxy with ID '" + resource.id + "'");
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
