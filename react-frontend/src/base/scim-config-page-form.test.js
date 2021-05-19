import React from 'react';

test("bla", () =>
{
    let field = {
        "fileUpload.keystoreFile": "MII1"
    }

    let scimResource = {};

    for (let [key, value] of Object.entries(field))
    {
        let parts = key.split(".")
        let currentObject = scimResource;
        for (let i = 0; i < parts.length - 1; i++)
        {
            currentObject = addField(scimResource, parts[i]);
        }
        currentObject[parts[parts.length - 1]] = value;
    }

    console.log(scimResource)
});

function addField(jsonObject, objectName)
{
    return jsonObject[objectName] = {};
}