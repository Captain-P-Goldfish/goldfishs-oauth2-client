import React from "react";
import ScimClient from "./scim-client";
import {Optional, toBase64} from "../services/utils";

export default class ScimComponent extends React.Component
{
    constructor(props)
    {
        super(props);
        this.scimClient = new ScimClient(props.scimResourcePath);
        this.errorListener = this.errorListener.bind(this);
        this.scimClient.setErrorListener(this.errorListener);

        this.edit = this.edit.bind(this);
        this.resetEditMode = this.resetEditMode.bind(this);
        this.getErrors = this.getErrors.bind(this);
        this.deleteEntry = this.deleteEntry.bind(this);
        this.onChange = this.onChange.bind(this);
        this.updateResource = this.updateResource.bind(this);
        this.showModal = this.showModal.bind(this);
        this.hideModal = this.hideModal.bind(this);
        this.resetErrors = this.resetErrors.bind(this);
        this.getErrorMessages = this.getErrorMessages.bind(this);
    }

    errorListener(errorsObject)
    {
        this.setState({
            errors: new Optional(errorsObject).orElse({})
        })
    }

    resetErrors()
    {
        this.setState({
            errors: {}
        })
    }

    edit()
    {
        this.setState({editMode: true});
    }

    resetEditMode()
    {
        this.setState({editMode: false});
    }

    showModal()
    {
        this.setState({showModal: true})
    }

    hideModal()
    {
        this.setState({showModal: false})
    }

    async onChange(name, value)
    {
        // this.scimClient.onChange(name, value);
        let addField = function (jsonObject, objectName)
        {
            if (jsonObject[objectName] === undefined || jsonObject[objectName] === null)
            {
                return jsonObject[objectName] = {};
            }
            return jsonObject[objectName];
        }

        let scimResource = this.state.resource;

        let parts = name.split(".")
        let currentObject = scimResource;
        for (let i = 0; i < parts.length - 1; i++)
        {
            currentObject = addField(currentObject, parts[i]);
        }
        if (typeof value.name == 'string')
        {
            currentObject[parts[parts.length - 1]] = await toBase64(value);
        }
        else
        {
            currentObject[parts[parts.length - 1]] = value;
        }
        this.setState({resource: scimResource})
    }

    getErrors(fieldName)
    {
        return new Optional(this.state.errors).map(val => val.fieldErrors).map(fieldErrors => fieldErrors[fieldName])
                                              .orElse([]);
    }

    getErrorMessages()
    {
        return new Optional(this.state.errors).map(val => val.errorMessages).orElse([]);
    }

    async deleteEntry(id)
    {
        if (id === undefined)
        {
            this.props.onDeleteSuccess(id);
            this.resetErrors()
            return;
        }
        let response = await this.scimClient.deleteResource(id);
        if (response.success)
        {
            this.props.onDeleteSuccess(id);
            this.resetErrors()
        }
    }

    async createResource()
    {
        let response = await this.scimClient.createResource(this.state.resource);
        if (response.success)
        {
            response.resource.then(resource =>
            {
                this.setState({resource: resource})
                this.props.onCreateSuccess(resource);
            })
            this.resetEditMode();
            this.resetErrors()
        }
    }

    async updateResource(id)
    {
        let response = await this.scimClient.updateResource(this.state.resource, id);
        if (response.success)
        {
            response.resource.then(resource =>
            {
                this.setState({resource: resource})
            })
            this.resetEditMode();
            this.resetErrors()
        }
    }
}