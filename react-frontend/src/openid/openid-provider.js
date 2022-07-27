import React, {createRef} from "react";
import ScimClient from "../scim/scim-client";
import {Optional} from "../services/utils";
import {Alert, Card, CardDeck, Nav, Table} from "react-bootstrap";
import {ArrowRightCircle, FileEarmarkPlus} from "react-bootstrap-icons";
import {
    CardControlIcons,
    CardDateRows,
    ErrorMessagesAlert,
    LoadingSpinner,
    ModifiableCardEntry
} from "../base/form-base";
import * as lodash from "lodash";
import ScimComponentBasics from "../scim/scim-component-basics";
import Form from "react-bootstrap/Form";
import {GoThumbsup} from "react-icons/go";
import Modal from "../base/modal";
import Button from "react-bootstrap/Button";
import {CardInputField} from "../base/card-base";
import {LinkContainer} from "react-router-bootstrap";

export default class OpenidProvider extends React.Component
{

    constructor(props)
    {
        super(props);
        this.state = {
            errors: {},
            providerList: [],
            currentPage: 0
        };
        this.scimResourcePath = "/scim/v2/OpenIdProvider";
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient(this.scimResourcePath, this.setState);
        this.addNewProvider = this.addNewProvider.bind(this);
        this.onUpdateSuccess = this.onUpdateSuccess.bind(this);
        this.onCreateSuccess = this.onCreateSuccess.bind(this);
        this.removeProvider = this.removeProvider.bind(this);
    }

    async componentDidMount()
    {
        let startIndex = (this.state.currentPage * this.props.serviceProviderConfig.filter.maxResults) + 1;
        let count = this.props.serviceProviderConfig.filter.maxResults;

        await this.scimClient.listResources({
            startIndex: startIndex,
            count: count,
            sortBy: 'name'
        }).then(listResponse =>
        {
            listResponse.resource.then(listResponse =>
            {
                let newResources = new Optional(listResponse.Resources).orElse([]);
                let oldResources = new Optional(this.state.providerList).orElse([]);
                let concatedResources = lodash.concat(oldResources, newResources);
                this.setState({
                    providerList: concatedResources,
                    errors: {}
                })
            })
        });
    }

    addNewProvider()
    {
        let providerList = [...this.state.providerList];
        const resource = providerList.filter(provider => provider.id === undefined);
        if (resource.length === 0)
        {
            providerList.unshift({});
            this.setState({
                providerList: providerList,
                newProvider: undefined,
                deletedProviderName: undefined
            })
        }
        else
        {
            this.setState({
                errors: {
                    errorMessages: ["There is already a new form available in the view."]
                },
                newProvider: undefined,
                deletedProviderName: undefined
            })
        }
    }

    onUpdateSuccess(provider)
    {
        let providerList = [...this.state.providerList];
        let oldProvider = lodash.find(providerList, p => p.id === provider.id);
        lodash.merge(oldProvider, provider);
        this.setState({
            providerList: providerList,
            newProvider: undefined,
            deletedProviderName: undefined
        })
    }

    onCreateSuccess(provider)
    {
        let providerList = [...this.state.providerList];
        let oldProvider = lodash.find(providerList, p => p.id === undefined);
        lodash.merge(oldProvider, provider);
        this.setState({
            providerList: providerList,
            newProvider: oldProvider,
            deletedProviderName: undefined
        })
    }

    removeProvider(id)
    {
        let providerList = [...this.state.providerList];
        let oldProvider = providerList.filter(provider => provider.id === id)[0];
        lodash.remove(providerList, provider => provider.id === id);
        this.setState({
            providerList: providerList,
            newProvider: undefined,
            deletedProviderName: oldProvider.name,
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
                <Alert id={"save-alert-success"} variant={"success"}
                       show={new Optional(this.state.newProvider).isPresent()}>
                    <Form.Text><GoThumbsup /> Provider with name
                                              '{new Optional(this.state.newProvider).map(provider => provider.name)
                                                                                    .orElse("-")}'
                                              was successfully created</Form.Text>
                </Alert>
                <Alert id="card-list-deletion-success"
                       variant={"success"}
                       show={new Optional(this.state.deletedProviderName).isPresent()}>
                    <Form.Text>
                        <GoThumbsup /> OpenID Provider "{this.state.deletedProviderName}" was successfully deleted
                    </Form.Text>
                </Alert>
                <ErrorMessagesAlert errors={this.state.errors} />
                <CardDeck>
                    {
                        this.state.providerList.map((provider) =>
                        {
                            return <OpenIdProviderCardEntry key={new Optional(provider.id).orElse("new")}
                                                            scimResourcePath={this.scimResourcePath}
                                                            provider={provider}
                                                            onCreateSuccess={this.onCreateSuccess}
                                                            onUpdateSuccess={this.onUpdateSuccess}
                                                            onDeleteSuccess={this.removeProvider} />
                        })
                    }
                </CardDeck>
            </React.Fragment>
        );
    }
}

class OpenIdProviderCardEntry extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {
            showModal: false,
            editMode: new Optional(props.provider).map(val => val.id).map(val => false).orElse(true),
            provider: JSON.parse(JSON.stringify(props.provider))
        }
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient(this.props.scimResourcePath, this.setState);
        this.formReference = createRef();

        this.scimComponentBasics = new ScimComponentBasics({
            scimClient: this.scimClient,
            formReference: this.formReference,
            getOriginalResource: () => this.props.provider,
            getCurrentResource: () => this.state.provider,
            setCurrentResource: resource => this.setState({provider: resource}),
            setState: this.setState,
            onCreateSuccess: this.props.onCreateSuccess,
            onUpdateSuccess: this.props.onUpdateSuccess,
            onDeleteSuccess: this.props.onDeleteSuccess
        });
    }


    render()
    {
        return (
            <Card id={"provider-card-" + this.state.provider.id} key={this.state.provider.id}
                  border={"warning"} bg={"dark"} className={"resource-card provider-card"}>
                <Nav className="flex-column">
                </Nav>

                <Form onSubmit={this.scimComponentBasics.onSubmit} ref={this.formReference}>

                    <Modal id={"delete-dialog-" + this.state.provider.id}
                           show={this.state.showModal}
                           variant="danger"
                           title={"Delete OpenID Provider with name '" + this.state.provider.name + "'"}
                           message="Are you sure?"
                           submitButtonText="delete"
                           onSubmit={this.scimComponentBasics.deleteResource}
                           cancelButtonText="cancel"
                           onCancel={() => this.scimComponentBasics.setStateValue("showModal", false)}>
                    </Modal>

                    <Alert id={"save-alert-success"} variant={"success"}
                           show={new Optional(this.state.success).orElse(false)}>
                        <Form.Text><GoThumbsup /> OpenID Provider was successfully updated</Form.Text>
                    </Alert>

                    <ErrorMessagesAlert errors={this.state.errors} />

                    <Card.Header id={"provider-card-header-" + this.state.provider.id}>
                        <div className={"card-name-header"}>
                            {
                                this.state.editMode === false &&
                                <React.Fragment>
                                    <LinkContainer exact
                                                   to={"/views/openIdProvider/" + this.props.provider.id + "/openIdClients"}>
                                        <a href={"/#"}>
                                            <h5>
                                                <ArrowRightCircle style={{color: "bisque", marginRight: "15px"}}
                                                                  size={"20px"} height={"30px"} />
                                                {this.state.provider.name}
                                            </h5>
                                        </a>
                                    </LinkContainer>
                                </React.Fragment>
                            }
                            {
                                this.state.editMode === true &&
                                <CardInputField
                                    value={new Optional(this.state.provider.name).orElse("")}
                                    type={"text"}
                                    id={"name-" + this.state.provider.id}
                                    name={"name"}
                                    placeholder={"OpenID Provider identifier"}
                                    onChange={this.scimComponentBasics.updateInput}
                                    onError={fieldName => this.scimClient.getErrors(
                                        this.state, fieldName)} />
                            }
                        </div>
                        <CardControlIcons resource={this.state.provider}
                                          spinner={<LoadingSpinner show={this.state.showSpinner} />}
                                          editMode={this.state.editMode}
                                          createResource={this.scimComponentBasics.createResource}
                                          updateResource={this.scimComponentBasics.updateResource}
                                          resetEditMode={this.scimComponentBasics.resetEditMode}
                                          edit={() => this.scimComponentBasics.setStateValue("editMode", true)}
                                          showModal={() => this.scimComponentBasics.setStateValue("showModal", true)} />
                        {/* this button enables pressing enter in the edit form */}
                        <Button id={"upload"} type="submit" hidden={true} />
                    </Card.Header>

                    <Card.Body>
                        <React.Fragment>
                            <Table size="sm" variant="dark" borderless striped>
                                <tbody>
                                    <ModifiableCardEntry header={"Discovery Endpoint"}
                                                         name={"discoveryEndpoint"}
                                                         resourceId={this.state.provider.id}
                                                         editMode={this.state.editMode}
                                                         resourceValue={this.state.provider.discoveryEndpoint}
                                                         placeholder={"The URL to the OpenID Providers discovery endpoint"}
                                                         onChange={this.scimComponentBasics.updateInput}
                                                         onError={fieldName => this.scimClient.getErrors(
                                                             this.state, fieldName)} />
                                    <CardDateRows resource={this.state.provider} />
                                </tbody>
                            </Table>
                        </React.Fragment>
                    </Card.Body>
                </Form>
            </Card>
        )
    }
}
