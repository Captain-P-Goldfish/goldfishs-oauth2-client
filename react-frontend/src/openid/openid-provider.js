import React from "react";
import ScimClient from "../scim/scim-client";
import {Optional} from "../services/utils";
import {ErrorMessageList} from "../base/config-page-form";
import {Alert, Card, CardDeck, Table} from "react-bootstrap";
import Modal from "../base/modal";
import {FileEarmarkPlus, PencilSquare, Save, TrashFill, XLg} from "react-bootstrap-icons";
import Spinner from "react-bootstrap/Spinner";
import {CardInputField} from "../base/card-base";
import ScimComponent from "../scim/scim-component";

class ProviderCardEntry extends ScimComponent
{

    constructor(props)
    {
        super(props);
        this.state = {
            showModal: false,
            editMode: new Optional(props.provider).map(val => val.id).map(val => false).orElse(true),
            resource: props.provider
        }
    }


    render()
    {
        let spinner;
        if (this.state.showSpinner)
        {
            spinner = <span style={{marginRight: 5 + 'px'}}>
                          <Spinner animation="border" variant="warning" size="sm" role="status" />
                      </span>;
        }

        let errorMessages = this.getErrorMessages();

        return (
            <Card id={"provider-card-" + this.state.resource.id} key={this.state.resource.id}
                  border={"warning"} bg={"dark"} className={"resource-card provider-card"}>
                <Alert id={this.formId + "-alert-error"} variant={"danger"}
                       show={errorMessages.length !== 0}>
                    <ErrorMessageList fieldErrors={errorMessages} backgroundClass={""} />
                </Alert>
                <Modal id={"delete-dialog-" + this.state.resource.id}
                       show={this.state.showModal}
                       variant="danger"
                       title={"Delete Provider '" + this.state.resource.name + "'"}
                       message="Are you sure?"
                       submitButtonText="delete"
                       onSubmit={() => this.deleteEntry(this.state.resource.id)}
                       cancelButtonText="cancel"
                       onCancel={this.hideModal}>
                </Modal>
                <Card.Header id={"provider-card-header-" + this.state.resource.id}>
                    <div className={"card-name-header"}>
                        {
                            !this.state.editMode &&
                            <h5>{this.state.resource.name}</h5>
                        }
                        {
                            this.state.editMode &&
                            <CardInputField value={this.state.resource.name}
                                            name={"name"}
                                            placeHolder={"The URL to the OpenID discovery endpoint"}
                                            onChange={this.onChange}
                                            onError={this.getErrors} />
                        }
                    </div>
                    <div className="card-control-icons">
                        {spinner}
                        {
                            this.state.editMode &&
                            <React.Fragment>
                                <Save title={"save"} id={"save-icon-" + this.state.resource.id}
                                      onClick={() =>
                                      {
                                          if (this.state.resource.id === undefined)
                                          {
                                              this.createResource()
                                          }
                                          else
                                          {
                                              this.updateResource(this.state.resource.id)
                                          }
                                      }}
                                      style={{marginRight: 5 + 'px'}} />
                                {
                                    this.state.resource.id !== undefined &&
                                    <XLg title={"reset-edit"} id={"reset-update-icon-" + this.state.resource.id}
                                         onClick={this.resetEditMode} style={{marginRight: 5 + 'px'}} />
                                }
                            </React.Fragment>
                        }
                        {
                            !this.state.editMode &&
                            <PencilSquare title={"edit"} id={"update-icon-" + this.state.resource.id}
                                          onClick={this.edit} style={{marginRight: 5 + 'px'}} />
                        }
                        <TrashFill title={"delete"} id={"delete-icon-" + this.state.resource.id}
                                   onClick={this.showModal} />
                    </div>
                </Card.Header>
                <Card.Body>
                    <React.Fragment>
                        <Table size="sm" variant="dark" borderless striped>
                            <tbody>
                                <tr>
                                    <th>Discovery Endpoint</th>
                                    <td id={"provider-card-" + this.state.resource.id + "-discovery-endpoint"}>
                                        {
                                            this.state.editMode &&
                                            <CardInputField value={this.state.resource.discoveryEndpoint}
                                                            name={"discoveryEndpoint"}
                                                            placeHolder={"The URL to the OpenID discovery endpoint"}
                                                            onChange={this.onChange}
                                                            onError={this.getErrors} />
                                        }
                                        {
                                            !this.state.editMode &&
                                            this.state.resource.discoveryEndpoint
                                        }
                                    </td>
                                </tr>
                                <tr>
                                    <th>Signature Verification Key</th>
                                    <td id={"provider-card-" + this.state.resource.id + "-sig-verification-key"}>
                                        {this.state.resource.signatureVerificationKey}
                                    </td>
                                </tr>
                                <tr>
                                    <th>Created</th>
                                    <td>
                                        {
                                            new Optional(this.state.resource).map(val => val.meta).map(
                                                val => val.created).map(val => new Date(val).toUTCString()).orElse(null)
                                        }
                                    </td>
                                </tr>
                                <tr>
                                    <th>LastModified</th>
                                    <td>
                                        {
                                            new Optional(this.state.resource).map(val => val.meta).map(
                                                val => val.lastModified).map(val => new Date(val).toUTCString()).orElse(
                                                null)
                                        }
                                    </td>
                                </tr>
                            </tbody>
                        </Table>
                    </React.Fragment>
                </Card.Body>
            </Card>
        );
    }

}

export default class OpenidProvider extends React.Component
{

    constructor(props)
    {
        super(props);
        this.scimClient = new ScimClient("/scim/v2/OpenIdProvider");
        this.state = {
            errors: {},
            providerList: [],
            currentPage: 0
        };
        this.errorListener = this.errorListener.bind(this);
        this.scimClient.setErrorListener(this.errorListener);
        this.removeProvider = this.removeProvider.bind(this);
        this.addNewProvider = this.addNewProvider.bind(this);
        this.onCreateSuccess = this.onCreateSuccess.bind(this);
    }

    errorListener(errorsObject)
    {
        this.setState({
            errors: new Optional(errorsObject).orElse({})
        })
    }

    async componentDidMount()
    {
        let startIndex = (this.state.currentPage * window.MAX_RESULTS) + 1;
        let count = window.MAX_RESULTS;

        await this.scimClient.listResources({
            startIndex: startIndex,
            count: count,
            sortBy: 'name'
        }).then(listResponse =>
        {
            listResponse.resource.then(listResponse =>
            {
                this.setState({
                    providerList: new Optional(listResponse.Resources).orElse([]),
                    errors: {}
                })
            })
        });
    }

    addNewProvider()
    {
        let providerList = this.state.providerList;
        const resource = providerList.filter(provider => provider.id === undefined);
        if (resource.length === 0)
        {
            providerList.unshift({});
            this.setState({
                providerList: providerList
            })
        }
        else
        {
            this.setState({
                errors: {
                    errorMessages: ["There is already a new form available in the view."]
                }
            })
        }
    }

    onCreateSuccess(provider)
    {
        this.removeProvider(undefined);
        let providerList = this.state.providerList;
        providerList.push(provider);
        providerList.sort((a, b) => (a.name > b.name) ? 1 : ((b.name > a.name) ? -1 : 0))

        this.setState({
            providerList: providerList
        })
    }

    removeProvider(id)
    {
        let providerList = this.state.providerList
        const newProviderList = providerList.filter(provider => provider.id !== id)
        this.setState({
            providerList: newProviderList,
            errors: {}
        })
    }

    render()
    {
        return (
            <React.Fragment>
                <p className={"add-new-resource"} onClick={this.addNewProvider}>
                    <span className={"add-new-resource"}>Add new Provider <br /><FileEarmarkPlus /></span>
                </p>
                <h2>
                    <span>OpenID Provider List</span>
                </h2>
                <Alert id={this.props.formId + "-alert-error"} variant={"danger"}
                       show={new Optional(this.state.errors).map(val => val.errorMessages).map(val => true).orElse(
                           false)}>
                    <ErrorMessageList fieldErrors={this.state.errors.errorMessages} backgroundClass={""} />
                </Alert>
                <CardDeck>
                    {
                        this.state.providerList.map((provider) =>
                        {
                            return <ProviderCardEntry key={new Optional(provider.id).orElse(-1)}
                                                      scimResourcePath={"/scim/v2/OpenIdProvider"}
                                                      provider={provider}
                                                      onDeleteSuccess={this.removeProvider}
                                                      onCreateSuccess={this.onCreateSuccess} />
                        })
                    }
                </CardDeck>
            </React.Fragment>
        );
    }
}