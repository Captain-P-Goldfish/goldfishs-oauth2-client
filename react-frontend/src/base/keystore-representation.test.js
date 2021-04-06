import React from 'react';
import {act, render} from '@testing-library/react';
import {unmountComponentAtNode} from "react-dom";
import Assertions, {mockFetch} from "../setupTests";
import KeystoreRepresentation from "./keystore-representation";


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

test("test keystore representation", async () => {
    const basePath = "/certificate";
    const certificateAliases = ["goldfish", "localhost", "unit-test"];
    const type = "Hello World";

    act(() => {
        render(<KeystoreRepresentation basePath={basePath}
                                       certificateAliases={[...certificateAliases]}
                                       type={type} />, container);
    });

    new Assertions("#application-certificate-info-header p").isPresent().isVisible()
        .assertEquals("Application " + type + " Infos");
    let downloadLinkAssertion = new Assertions("#keystore-download-link").isPresent().isVisible()
        .assertEquals("Download");
    expect(downloadLinkAssertion.element.href).toBe("http://localhost" + basePath + "/download")
    new Assertions("#card-list-infos-alert").isPresent().isVisible()
        .assertEquals('Application ' + type + ' contains "' + certificateAliases.length + '" entries');
    new Assertions("#card-list-deletion-success").isNotPresent();

    let counter = 0;
    for (let certificateAlias of certificateAliases) {
        counter++;
        let deleteButtonAssertion;
        await new Assertions("#delete-icon-" + certificateAlias).isPresent().isVisible()
            .clickElement(() => {
                deleteButtonAssertion = new Assertions("#delete-dialog-" + certificateAlias + "-button-accept")
                    .isPresent().isVisible();
            })

        mockFetch(200, {});

        await new Assertions("#load-certificate-data-button-for-" + certificateAlias).isPresent().isVisible()
            .clickElement(() => {
                new Assertions("#load-certificate-data-button-for-" + certificateAlias).isNotPresent();
            });

        expect(global.fetch).toBeCalledTimes(1);
        expect(global.fetch).toBeCalledWith(basePath + "/load-alias?alias=" + certificateAlias)
        // remove the mock to ensure tests are completely isolated
        global.fetch.mockRestore();

        mockFetch(204, null);

        await deleteButtonAssertion.clickElement(() => {
            new Assertions("#alias-card-" + certificateAlias).isNotPresent();
        })

        expect(global.fetch).toBeCalledTimes(1);
        expect(global.fetch).toBeCalledWith(basePath + "/delete-alias?alias=" + certificateAlias,
            {method: "DELETE"})
        // remove the mock to ensure tests are completely isolated
        global.fetch.mockRestore();

        new Assertions("#card-list-infos-alert").isPresent().isVisible()
            .assertEquals('Application ' + type + ' contains "' + (certificateAliases.length - counter)
                + '" entries');
        new Assertions("#card-list-deletion-success").isPresent().isVisible()
            .assertEquals('Key entry for alias "' + certificateAlias + '" was successfully deleted');
    }
    expect(counter).toBe(certificateAliases.length);
    expect(certificateAliases.length).toBe(3);
    new Assertions("#card-list-infos-alert").isPresent().isVisible()
        .assertEquals('Application ' + type + ' contains "0" entries');
})