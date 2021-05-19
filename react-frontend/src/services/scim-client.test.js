import ScimClient from "./scim-client";
import {mockFetch} from "../setupTests";

test("get resource", () =>
{
    const baseUrl = "/Keystore";
    const id = "1"
    let scimClient = new ScimClient(baseUrl);

    mockFetch(200, {});

    scimClient.getResource(id);

    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith(baseUrl + "/" + id, {"method": "GET"})
    global.fetch.mockRestore();
});

test("list without any params", () =>
{
    const baseUrl = "/Keystore";
    let scimClient = new ScimClient(baseUrl);

    mockFetch(200, {});

    scimClient.listResources();

    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith(baseUrl, {"method": "GET"})
    global.fetch.mockRestore();
});

test("list with all params", () =>
{
    const baseUrl = "/Keystore";
    let scimClient = new ScimClient(baseUrl);

    mockFetch(200, {});

    scimClient.listResources({
        startIndex: 1,
        count: 50,
        filter: "userName eq \"hello\"",
        sortBy: "userName",
        sortOrder: "ascending",
        attributes: "displayName",
        excludedAttributes: "nickName"
    });

    let expectedQuery = Array.of("startIndex=1", "count=50", "filter=" + encodeURI("userName eq \"hello\""),
        "sortBy=userName", "sortOrder=ascending", "attributes=displayName", "excludedAttributes=nickName").join("&");

    expect(global.fetch).toBeCalledTimes(1);
    expect(global.fetch).toBeCalledWith(baseUrl + "?" + expectedQuery, {"method": "GET"})
    global.fetch.mockRestore();
});