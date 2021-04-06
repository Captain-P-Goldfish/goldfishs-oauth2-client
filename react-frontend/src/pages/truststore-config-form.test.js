import React from 'react';
import {act, render, waitFor} from '@testing-library/react';
import Assertions, {mockFetch} from "../setupTests"
import {unmountComponentAtNode} from "react-dom";
import TruststoreConfigForm from "./truststore-config-form";

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
    mockFetch(200, []);
    new Assertions("#keystore-certificate-entries").isNotPresent();

    act(() => {
        render(<TruststoreConfigForm />, container);
    });

    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/truststore/infos")
    global.fetch.mockRestore();
}

/* ********************************************************************************************************* */

function getFakeKeystoreInfos() {
    return {
        "numberOfEntries": 3,
        "certificateAliases": ["goldfish", "localhost", "unit-test"]
    };
}

/* ********************************************************************************************************* */

test("verify certificate data is displayed", async () => {
    const fakeKeystoreInfos = getFakeKeystoreInfos();

    mockFetch(200, fakeKeystoreInfos);

    new Assertions("#keystore-certificate-entries").isNotPresent();

    act(() => {
        render(<TruststoreConfigForm />, container);
    });
    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith("/truststore/infos")
    global.fetch.mockRestore();

    await waitFor(() => {
        new Assertions("#keystore-certificate-entries").isPresent();
        new Assertions("#card-list-infos-alert").isPresent().isVisible()
            .assertEquals('Application Truststore contains "3" entries');
    });
});

/* ********************************************************************************************************* */

test("Upload truststore", async () => {
    loadPageWithoutEntries();
    new Assertions("#card-list-infos-alert").isPresent().isVisible()
        .assertEquals('Application Truststore contains "0" entries');

    // handle file input field
    {
        const truststoreFile = new File(["hello world"], "cacerts");
        const truststorePassword = "123456";
        new Assertions("#truststoreFile").isPresent().isVisible().fireChangeEvent(truststoreFile);
        new Assertions("#truststorePassword").isPresent().isVisible().fireChangeEvent(truststorePassword);

        mockFetch(200, {
            aliases: ["goldfish"],
            duplicateAliases: ["unit-test"],
            duplicateCertificates: ["localhost"]
        })

        await new Assertions("#truststoreUploadButton").isPresent().isVisible()
            .clickElement(() => {
                new Assertions("#truststoreUploadForm-alert-success").isPresent().isVisible()
                    .assertEquals("Truststore was successfully merged into application keystore");
            });
        new Assertions("#upload-form-alert-duplicate-aliases").isPresent().isVisible()
            .assertEquals("The following aliases could not be added because the alias is " +
                "duplicated.Number of not added aliases: 1 [unit-test]");
        new Assertions("#upoad-form-alert-duplicate-certificates").isPresent().isVisible()
            .assertEquals("The following aliases could not be added because the certificate is already " +
                "present: [localhost]");

        expect(global.fetch).toBeCalledTimes(1);
        let formData = new FormData();
        formData.append("truststoreFile", truststoreFile)
        formData.append("truststorePassword", truststorePassword)
        expect(global.fetch).toBeCalledWith("/truststore/add", {
            method: "POST",
            body: formData
        })
        global.fetch.mockRestore();

       new Assertions("#card-list-infos-alert").isPresent().isVisible()
           .assertEquals('Application Truststore contains "1" entries');
    }
});

/* ********************************************************************************************************* */

test("Upload certificate", async () => {
    loadPageWithoutEntries();
    new Assertions("#card-list-infos-alert").isPresent().isVisible()
        .assertEquals('Application Truststore contains "0" entries');

    // handle file input field
    {
        const certificateFile = new File(["hello world"], "cacerts");
        const alias = "goldfish";
        new Assertions("#certificateFile").isPresent().isVisible().fireChangeEvent(certificateFile);
        new Assertions("#alias").isPresent().isVisible().fireChangeEvent(alias);

        mockFetch(200, {
            aliases: ["goldfish"]
        })

        await new Assertions("#certFileUploadButton").isPresent().isVisible()
            .clickElement(() => {
                new Assertions("#certUploadForm-alert-success").isPresent().isVisible()
                    .assertEquals("Certificate was successfully added to application keystore");
            });
        new Assertions("#upload-form-alert-duplicate-aliases").isNotPresent()
        new Assertions("#upoad-form-alert-duplicate-certificates").isNotPresent();

        expect(global.fetch).toBeCalledTimes(1);
        let formData = new FormData();
        formData.append("certificateFile", certificateFile)
        formData.append("alias", alias)
        expect(global.fetch).toBeCalledWith("/truststore/add", {
            method: "POST",
            body: formData
        })
        global.fetch.mockRestore();

        new Assertions("#card-list-infos-alert").isPresent().isVisible()
            .assertEquals('Application Truststore contains "1" entries');
    }
});

/* ********************************************************************************************************* */
