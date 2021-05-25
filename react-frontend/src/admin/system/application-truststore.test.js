import React from 'react';
import {act, render, waitFor} from '@testing-library/react';
import Assertions, {mockFetch} from "../../setupTests"
import {unmountComponentAtNode} from "react-dom";
import ApplicationTruststore from "./application-truststore";
import {toBase64} from "../../services/utils";

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

function loadPageWithoutEntries()
{
    mockFetch(200, {Resources: []});
    new Assertions("#keystore-certificate-entries").isNotPresent();

    act(() =>
    {
        render(<ApplicationTruststore />, container);
    });

    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/Truststore", {method: "GET"})
    global.fetch.mockRestore();
}

/* ********************************************************************************************************* */

function getFakeKeystoreInfos()
{
    return {
        "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
        "totalResults": 1,
        "itemsPerPage": 1,
        "startIndex": 1,
        "Resources": [{
            "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:Truststore"],
            "id": "1",
            "aliases": ["unit-test", "goldfish", "localhost"],
            "meta": {
                "resourceType": "Truststore",
                "created": "2021-05-19T14:32:17.952Z",
                "lastModified": "2021-05-19T14:32:17.952Z",
                "location": "http://localhost:55402/scim/v2/Truststore/1"
            }
        }]
    };
}

/* ********************************************************************************************************* */

test("verify certificate data is displayed", async () =>
{
    const fakeKeystoreInfos = getFakeKeystoreInfos();

    mockFetch(200, fakeKeystoreInfos);

    new Assertions("#keystore-certificate-entries").isNotPresent();

    act(() =>
    {
        render(<ApplicationTruststore />, container);
    });
    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/scim/v2/Truststore", {method: "GET"})
    global.fetch.mockRestore();

    await waitFor(() =>
    {
        new Assertions("#keystore-certificate-entries").isPresent();
        new Assertions("#card-list-infos-alert").isPresent().isVisible()
                                                .assertEquals(
                                                    'Application Truststore contains "3" entries');
    });
});

/* ********************************************************************************************************* */

test("Upload truststore", async () =>
{
    loadPageWithoutEntries();
    new Assertions("#card-list-infos-alert").isPresent().isVisible()
                                            .assertEquals('Application Truststore contains "0" entries');

    // handle file input field
    {
        const truststoreFile = new File(["hello world"], "cacerts");
        const truststorePassword = "123456";
        new Assertions("#truststoreUpload\\.truststoreFile").isPresent().isVisible().fireChangeEvent(truststoreFile);
        new Assertions("#truststoreUpload\\.truststorePassword").isPresent().isVisible().fireChangeEvent(
            truststorePassword);

        mockFetch(201, {
            "id": "1",
            "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:Truststore"],
            "truststoreUploadResponse": {
                "aliases": ["goldfish"],
                "duplicateAliases": ["unit-test"],
                "duplicateCertificateAliases": ["localhost"]
            },
            "meta": {
                "resourceType": "Truststore",
                "created": "2021-05-19T14:42:14.055Z",
                "lastModified": "2021-05-19T14:42:14.055Z",
                "location": "http://localhost:55785/scim/v2/Truststore/1"
            }
        })

        await new Assertions("#uploadTruststore").isPresent().isVisible().clickElement(() =>
        {
            new Assertions("#truststoreUploadForm-alert-success").isPresent().isVisible()
                                                                 .assertEquals(
                                                                     "Truststore was successfully merged");
        });
        new Assertions("#upload-form-alert-duplicate-aliases").isPresent().isVisible().assertEquals(
            "The following aliases could not be added because the alias is duplicated.Number "
            + "of not added aliases: 1 [unit-test]");
        new Assertions("#upoad-form-alert-duplicate-certificates").isPresent().isVisible().assertEquals(
            "The following aliases could not be added because the certificate is already present: [localhost]");


        let data = {
            "truststoreUpload": {
                "truststoreFile": await toBase64(truststoreFile),
                "truststorePassword": truststorePassword
            }
        };
        expect(global.fetch).toBeCalledTimes(1);
        expect(global.fetch).toBeCalledWith("/scim/v2/Truststore",
            {
                method: "POST",
                headers: {'Content-Type': 'application/scim+json'},
                body: JSON.stringify(data)
            })
        global.fetch.mockRestore();

        new Assertions("#card-list-infos-alert").isPresent().isVisible()
                                                .assertEquals('Application Truststore contains "1" entries');
    }
});

/* ********************************************************************************************************* */

test("Upload certificate", async () =>
{
    loadPageWithoutEntries();
    new Assertions("#card-list-infos-alert").isPresent().isVisible()
                                            .assertEquals('Application Truststore contains "0" entries');

    // handle file input field
    {
        const certificateFile = new File(["hello world"], "cacerts");
        const alias = "goldfish";
        new Assertions("#certificateUpload\\.certificateFile").isPresent().isVisible().fireChangeEvent(certificateFile);
        new Assertions("#certificateUpload\\.alias").isPresent().isVisible().fireChangeEvent(alias);

        mockFetch(201, {
            "id": "1",
            "schemas": ["urn:ietf:params:scim:schemas:captaingoldfish:2.0:Truststore"],
            "certificateUploadResponse": {
                "alias": "goldfish"
            },
            "meta": {
                "resourceType": "Truststore",
                "created": "2021-05-19T14:42:14.055Z",
                "lastModified": "2021-05-19T14:42:14.055Z",
                "location": "http://localhost:55785/scim/v2/Truststore/1"
            }
        })

        await new Assertions("#uploadCertificate").isPresent().isVisible().clickElement(() =>
        {
            new Assertions("#certificateUploadForm-alert-success").isPresent().isVisible().assertEquals(
                "Entry with alias 'goldfish' was successfully added");
        });
        new Assertions("#upload-form-alert-duplicate-aliases").isNotPresent()
        new Assertions("#upoad-form-alert-duplicate-certificates").isNotPresent();


        let data = {
            "certificateUpload": {
                "certificateFile": await toBase64(certificateFile),
                "alias": alias
            }
        };
        expect(global.fetch).toBeCalledTimes(1);
        expect(global.fetch).toBeCalledWith("/scim/v2/Truststore",
            {
                method: "POST",
                headers: {'Content-Type': 'application/scim+json'},
                body: JSON.stringify(data)
            })
        global.fetch.mockRestore();

        new Assertions("#card-list-infos-alert").isPresent().isVisible()
                                                .assertEquals('Application Truststore contains "1" entries');
    }
});

/* ********************************************************************************************************* */
