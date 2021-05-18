import React from 'react';
import KeystoreForm from "./keystore-config-form";
import {act, fireEvent, render, waitFor} from '@testing-library/react';
import Assertions, {mockFetch} from "../setupTests"
import {unmountComponentAtNode} from "react-dom";
import {toBase64} from "../services/utils";


jest.setTimeout(300000)

let container = null;

/* ********************************************************************************************************* */

beforeEach(() => {
    // setup a DOM element as a render target
    container = document.createElement("div");
    document.body.appendChild(container);
});

/* ********************************************************************************************************* */

afterEach(() => {
    // cleanup on exiting
    unmountComponentAtNode(container);
    container.remove();
    container = null;
});

/* ********************************************************************************************************* */

function loadPageWithoutEntries() {
    mockFetch(200, {
        Resources: []
    });
    new Assertions("#keystore-certificate-entries").isNotPresent();

    act(() => {
        render(<KeystoreForm />, container);
    });
    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/Keystore", {method: "GET"})
    global.fetch.mockRestore();
}

/* ********************************************************************************************************* */

function getFakeKeystoreInfos() {
    return {
        "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
        "totalResults": 1,
        "itemsPerPage": 1,
        "startIndex": 1,
        "Resources": [{
            "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:Keystore"],
            "id": "1",
            "aliases": ["unit-test", "goldfish", "localhost"],
            "meta": {
                "resourceType": "Keystore",
                "created": "2021-05-18T19:47:21.210Z",
                "lastModified": "2021-05-18T19:47:21.210Z",
                "location": "http://localhost:59478/scim/v2/Keystore/1"
            }
        }]
    };
}

/* ********************************************************************************************************* */

test("verify certificate data is displayed", async () => {
    const fakeKeystoreInfos = getFakeKeystoreInfos();

    mockFetch(200, fakeKeystoreInfos);

    new Assertions("#keystore-certificate-entries").isNotPresent();

    act(() => {
        render(<KeystoreForm />, container);
    });
    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/Keystore", {method: "GET"})
    global.fetch.mockRestore();

    await waitFor(() => {
        new Assertions("#keystore-certificate-entries").isPresent();
        new Assertions("#card-list-infos-alert").isPresent().isVisible()
            .assertEquals('Application Keystore contains "3" entries');
    });
});

/* ********************************************************************************************************* */

test("Load page without any key entries", async () => {
    loadPageWithoutEntries();
    new Assertions("#card-list-infos-alert").isPresent().isVisible()
        .assertEquals('Application Keystore contains "0" entries');
});

/* ********************************************************************************************************* */

test("upload Keystore entries", async () => {
    loadPageWithoutEntries();
    const filename = "myKeystore.jks";
    const keystoreFile = new File([new Blob(['hello-world'], {type: 'text/plain'})], filename);
    const keystorePassword = "123456";
    const aliasOverride = "hello-world";
    const privateKeyPassword = "123456";
    const stateId = "34c8dc36-1543-4aac-97a1-16fb43b451f7";

    // verify forms are enabled or disabled
    {
        new Assertions("#uploadForm").isPresent().isVisible().hasNotClass("disabled");
        new Assertions("#aliasSelectionForm").isPresent().isVisible().hasClass("disabled");
    }

    // handle file input field
    {
        await new Assertions("#fileUpload\\.keystoreFile").isPresent().isVisible().fireChangeEvent(keystoreFile)
    }
    // handle keystore password input field
    {
        await new Assertions("#fileUpload\\.keystorePassword").isPresent().isVisible().assertEquals("")
            .fireChangeEvent(keystorePassword);
    }

    // mock fetch
    {
        mockFetch(200, {
            "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:Keystore"],
            "id": "1",
            "aliasSelection": {
                "stateId": stateId,
                "aliases": ["unit-test", "goldfish", "localhost"]
            },
            "meta": {
                "resourceType": "Keystore",
                "created": "2021-05-18T19:59:53.912Z",
                "lastModified": "2021-05-18T19:59:53.912Z",
                "location": "http://localhost:59926/scim/v2/Keystore/1"
            }
        });
    }

    // submit form
    {
        new Assertions("#uploadForm").isPresent().hasNotClass("disabled");
        new Assertions("#aliasSelectionForm").isPresent().hasClass("disabled");

        await new Assertions("#uploadButton")
            .isPresent()
            .isVisible()
            .assertEquals("Upload")
            .clickElement(() => {
                new Assertions("#uploadForm-alert-success").isPresent().isVisible()
                    .assertEquals("Keystore was successfully uploaded");
                new Assertions("#uploadForm").hasClass("disabled");
                new Assertions("#aliasSelectionForm").hasNotClass("disabled");
            });

        let data = {
            "fileUpload": {
                "keystoreFile": await toBase64(keystoreFile),
                "keystorePassword": keystorePassword
            }
        };
        expect(global.fetch).toBeCalledTimes(1);
        expect(global.fetch).toBeCalledWith("/scim/v2/Keystore",
            {
                method: "POST",
                headers: {'Content-Type': 'application/scim+json'},
                body: JSON.stringify(data)
            })
        global.fetch.mockRestore();
    }

    // check values of alias selection box
    {
        const aliasesElement = new Assertions("#aliasSelection\\.aliases").isPresent().isVisible().element;
        expect(aliasesElement.options.length).toBe(3);
        expect(aliasesElement.options[0].textContent).toBe("unit-test");
        expect(aliasesElement.options[1].textContent).toBe("goldfish");
        expect(aliasesElement.options[2].textContent).toBe("localhost");
    }

    // verify that first entry is selected and enter data to input fields
    {
        new Assertions("#aliasSelection\\.aliases")
            .isPresent().isVisible().hasValueSelected("unit-test");

        const aliasOverrideAssertion = new Assertions("#aliasSelection\\.aliasOverride").isPresent().isVisible();
        fireEvent.change(aliasOverrideAssertion.element, {target: {value: aliasOverride}})

        const privateKeyPasswordAssertion = new Assertions("#aliasSelection\\.privateKeyPassword").isPresent().isVisible();
        fireEvent.change(privateKeyPasswordAssertion.element, {target: {value: privateKeyPassword}})
    }

    // mock fetch
    {
        mockFetch(201, {
            "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:Keystore",
                "urn:ietf:params:scim:schemas:captaingoldfish:2.0:CertificateInfo"],
            "id": "1",
            "urn:ietf:params:scim:schemas:captaingoldfish:2.0:CertificateInfo": {
                "alias": "unit-test",
                "info": {
                    "issuerDn": "CN=unit-test",
                    "subjectDn": "CN=unit-test",
                    "sha256Fingerprint": "1e90dcc6e12aecf6529273b9627126d20ac6d14fb2bdd1dcbfd26baf7012c5bb",
                    "validFrom": "2021-03-30T18:12:01.000Z",
                    "validTo": "2121-03-30T18:12:01.000Z"
                }
            },
            "meta": {
                "resourceType": "Keystore",
                "created": "2021-05-18T20:26:26.326Z",
                "lastModified": "2021-05-18T20:26:26.326Z",
                "location": "http://localhost:60841/scim/v2/Keystore/1"
            }
        });
    }

    // click save button
    {
        await new Assertions("#saveButton").isPresent().isVisible().clickElement(() => {
            new Assertions("#aliasSelectionForm-alert-success").isPresent().isVisible()
                .assertEquals("Key Entry was successfully added");
            new Assertions("#alias-card-unit-test").isPresent().isVisible();
        })
        expect(global.fetch).toBeCalledTimes(1);
        let data = {
            "aliasSelection": {
                "stateId": stateId,
                "aliases": "unit-test",
                "aliasOverride": aliasOverride,
                "privateKeyPassword": privateKeyPassword
            }
        };

        expect(global.fetch).toBeCalledWith("/scim/v2/Keystore",
            {
                method: "POST",
                headers: {'Content-Type': 'application/scim+json'},
                body: JSON.stringify(data)
            });
        global.fetch.mockRestore();
    }
});


/* ********************************************************************************************************* */
