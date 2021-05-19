import React from 'react';
import {act, render} from '@testing-library/react';
import {unmountComponentAtNode} from "react-dom";
import Assertions, {mockFetch} from "../setupTests";
import CertificateList, {CertificateCardEntry} from "./certificate-list";


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

test("test certificate-card-entry representation", async () =>
{
    const loadUrl = "/certificate/load";
    const deleteUrl = "/certificate/delete";
    const certAlias = "goldfish";
    const onDeleteSuccess = jest.fn();

    act(() =>
    {
        render(<CertificateCardEntry loadUrl={loadUrl + "?alias=" + encodeURI(certAlias)}
                                     deleteUrl={deleteUrl + "?alias=" + encodeURI(certAlias)}
                                     alias={certAlias}
                                     onDeleteSuccess={onDeleteSuccess} />, container);
    });

    new Assertions("#alias-name-" + certAlias).isPresent().isVisible().assertEquals(certAlias);
    const loadCertDataButtonAssertion = new Assertions("#load-certificate-data-button-for-" + certAlias)
        .isPresent().isVisible();
    expect(loadCertDataButtonAssertion.element.previousSibling.tagName).toBe("IMG");

    const issuerDn = "CN=goldfish-root";
    const subjectDn = "CN=goldfish";
    const sha256 = "eafbea8af66e666310d6f73899d45a39a876b80037af7ae5fac2143ece6c9cee";
    const validFrom = "2021-03-27T18:42:14Z";
    const validUntil = "2121-03-27T18:42:14Z";
    const certInfo = {
        "issuerDn": issuerDn,
        "subjectDn": subjectDn,
        "sha256fingerprint": sha256,
        "validFrom": validFrom,
        "validUntil": validUntil
    };

    new Assertions("#issuer-dn-" + certAlias).isNotPresent();
    new Assertions("#subject-dn-" + certAlias).isNotPresent();
    new Assertions("#sha-256-" + certAlias).isNotPresent();
    new Assertions("#valid-from-" + certAlias).isNotPresent();
    new Assertions("#valid-until-" + certAlias).isNotPresent();

    mockFetch(200, certInfo);
    await loadCertDataButtonAssertion.clickElement(() =>
    {
        new Assertions("#issuer-dn-" + certAlias).isPresent().isVisible();
    })
    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith(loadUrl + "?alias=" + certAlias)
    global.fetch.mockRestore();

    new Assertions("#issuer-dn-" + certAlias).isVisible().assertEquals(issuerDn);
    new Assertions("#subject-dn-" + certAlias).isVisible().assertEquals(subjectDn);
    new Assertions("#sha-256-" + certAlias).isVisible().assertEquals(sha256);
    new Assertions("#valid-from-" + certAlias).isVisible().assertEquals(validFrom);
    new Assertions("#valid-until-" + certAlias).isVisible().assertEquals(validUntil);

    // click the delete icon and verify that the delete dialog is shown
    {
        const baseId = "#delete-dialog-" + certAlias;
        new Assertions(baseId).isNotPresent();
        let deleteIconAssertion = new Assertions("#delete-icon-" + certAlias);
        await deleteIconAssertion.clickElement(() =>
        {
            new Assertions("#delete-dialog-" + certAlias + "-header").isPresent().isVisible()
                                                                     .assertEquals("Delete '" + certAlias + "'");
            new Assertions("#delete-dialog-" + certAlias + "-text").isPresent().isVisible()
                                                                   .assertEquals("Are you sure?");
        })

        // cancel deletion
        {
            const cancelButtonAssertion = new Assertions(baseId + "-button-cancel").isPresent()
                                                                                   .isVisible().assertEquals("cancel");
            await cancelButtonAssertion.clickElement(() => new Assertions(baseId).isNotPresent());
        }

        // show delete dialog again
        await deleteIconAssertion.clickElement(() => new Assertions(baseId).isPresent().isVisible());

        // accept deletion
        {
            const deleteButtonAssertion = new Assertions(baseId + "-button-accept").isPresent()
                                                                                   .isVisible().assertEquals("delete");
            mockFetch(204, null);
            expect(onDeleteSuccess).toBeCalledTimes(0);
            await deleteButtonAssertion.clickElement(() =>
            {
                new Assertions("alias-card-" + certAlias).isNotPresent();
            });
            expect(global.fetch).toBeCalledTimes(1);
            expect(global.fetch).toBeCalledWith(deleteUrl + "?alias=" + certAlias,
                {method: "DELETE"})
            // remove the mock to ensure tests are completely isolated
            global.fetch.mockRestore();
            expect(onDeleteSuccess).toBeCalledTimes(1);
        }
    }
})

/* ********************************************************************************************************* */

test("test certificate-list representation", () =>
{
    const loadUrl = "/certificate/load";
    const deleteUrl = "/certificate/delete";
    const certificateAliases = ["goldfish", "localhost", "unit-test"];
    const onDeleteSuccess = jest.fn();

    act(() =>
    {
        render(<CertificateList certificateAliases={[...certificateAliases]}
                                loadUrl={loadUrl}
                                deleteUrl={deleteUrl}
                                onDeleteSuccess={onDeleteSuccess} />, container);
    });

    let cardDeckAssertion = new Assertions("#keystore-certificate-entries").isPresent().isVisible();
    let cards = cardDeckAssertion.element.children;
    expect(cards.length).toBe(certificateAliases.length);
})