import React from "react";
import ScimClient from "./scim-client";
import {Optional, toBase64} from "../services/utils";
import {PencilSquare, Save, TrashFill, XLg} from "react-bootstrap-icons";

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
        this.createResource = this.createResource.bind(this);
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
            new Optional(this.props.onDeleteSuccess).ifPresent(method => method(id));
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
                new Optional(this.props.onCreateSuccess).ifPresent(method => method(resource));
            })
            this.resetEditMode();
            this.resetErrors();
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
                new Optional(this.props.onUpdateSuccess).ifPresent(method => method(resource));
            })
            this.resetEditMode();
            this.resetErrors()
        }
    }
}

export function CardControlIcons(props)
{
    return (
        <div className="card-control-icons">
            {props.spinner}
            {
                props.editMode &&
                <React.Fragment>
                    <Save title={"save"} id={"save-icon-" + props.resource.id}
                          onClick={() =>
                          {
                              if (props.resource.id === undefined)
                              {
                                  props.createResource()
                              }
                              else
                              {
                                  props.updateResource(props.resource.id)
                              }
                          }}
                          style={{marginRight: 5 + 'px'}} />
                    {
                        props.resource.id !== undefined &&
                        <XLg title={"reset-edit"} id={"reset-update-icon-" + props.resource.id}
                             onClick={props.resetEditMode} style={{marginRight: 5 + 'px'}} />
                    }
                </React.Fragment>
            }
            {
                !props.editMode &&
                <PencilSquare title={"edit"} id={"update-icon-" + props.resource.id}
                              onClick={props.edit} style={{marginRight: 5 + 'px'}} />
            }
            <TrashFill title={"delete"} id={"delete-icon-" + props.resource.id}
                       onClick={props.showModal} />
        </div>
    );
}

export function CardDateRows(props)
{
    return (
        <React.Fragment>
            <tr>
                <th>Created</th>
                <td>
                    {
                        new Optional(props.resource).map(val => val.meta).map(
                            val => val.created).map(val => new Date(val).toUTCString()).orElse(null)
                    }
                </td>
            </tr>
            <tr>
                <th>LastModified</th>
                <td>
                    {
                        new Optional(props.resource).map(val => val.meta).map(
                            val => val.lastModified).map(val => new Date(val).toUTCString()).orElse(
                            null)
                    }
                </td>
            </tr>
        </React.Fragment>
    );
}