import React, {createRef} from "react";
import ScimClient from "../scim/scim-client";
import {Optional} from "../services/utils";
import * as lodash from "lodash";
import {ArrowLeftCircle, ArrowRightCircle, FileEarmarkPlus} from "react-bootstrap-icons";
import {Alert, Card, CardDeck, Table} from "react-bootstrap";
import Form from "react-bootstrap/Form";
import {GoThumbsup} from "react-icons/go";
import {
    CardControlIcons,
    CardDateRows,
    CardListSelector,
    CardRadioSelector,
    ErrorMessagesAlert,
    HiddenCardEntry,
    LoadingSpinner,
    ModifiableCardEntry
} from "../base/form-base";
import ScimComponentBasics from "../scim/scim-component-basics";
import Modal from "../base/modal";
import Button from "react-bootstrap/Button";
import {LinkContainer} from "react-router-bootstrap";
import {ApplicationInfoContext} from "../app";


export default class OpenidClients extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {
            errors: {},
            clientList: [],
            aliases: [],
            currentPage: 0,
            keyInfos: [],
            provider: {}
        };
        this.scimResourcePath = "/scim/v2/OpenIdClient";
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient(this.scimResourcePath, this.setState);
        this.addNewClient = this.addNewClient.bind(this);
        this.onUpdateSuccess = this.onUpdateSuccess.bind(this);
        this.onCreateSuccess = this.onCreateSuccess.bind(this);
        this.removeClient = this.removeClient.bind(this);
    }

    async componentDidMount()
    {
        let startIndex = (this.state.currentPage * this.props.serviceProviderConfig.filter.maxResults) + 1;
        let count = this.props.serviceProviderConfig.filter.maxResults;

        let openIdProviderId = this.props.match.params.id;
        let openIdProviderResourcePath = "/scim/v2/OpenIdProvider";
        await this.scimClient.getResource(openIdProviderId, openIdProviderResourcePath).then(response =>
        {
            if (response.success)
            {
                response.resource.then(openIdProvider =>
                {
                    this.setState({provider: openIdProvider});
                })
            }
            else
            {
                response.resource.then(errorResponse =>
                {
                    this.setState({
                        errors: {
                            errorMessages: [errorResponse.detail]
                        }
                    })
                })
            }
        })

        await this.scimClient.listResources({
            startIndex: startIndex,
            count: count,
            sortBy: 'clientId'
        }).then(listResponse =>
        {
            listResponse.resource.then(listResponse =>
            {
                let newResources = new Optional(listResponse.Resources).orElse([]);
                let oldResources = new Optional(this.state.clientList).orElse([]);
                let concatedResources = lodash.concat(oldResources, newResources);
                this.setState({
                    clientList: concatedResources,
                    errors: {}
                })
            })
        });

        this.loadKeystoreInfos();
    }

    async loadKeystoreInfos()
    {
        let keystoreResourcePath = "/scim/v2/Keystore";
        this.scimClient.getResource(undefined, keystoreResourcePath).then(response =>
        {
            if (response.success)
            {
                response.resource.then(listResponse =>
                {
                    this.setState({
                        keyInfos: new Optional(listResponse).map(lr => lr.Resources).map(r => r[0]).map(
                            keystore => keystore.keyInfos).orElse([])
                    });
                })
            }
            else
            {
                response.resource.then(errorResponse =>
                {
                    this.setState({
                        errors: {
                            errorMessages: [errorResponse.detail]
                        }
                    })
                })
            }
        })
    }

    addNewClient()
    {
        let clientList = [...this.state.clientList];
        const resource = clientList.filter(client => client.id === undefined);
        if (resource.length === 0)
        {
            clientList.unshift({});
            this.setState({
                clientList: clientList,
                newClient: undefined,
                deletedClientId: undefined
            })
        }
        else
        {
            this.setState({
                errors: {
                    errorMessages: ["There is already a new form available in the view."]
                },
                newClient: undefined,
                deletedClientId: undefined
            })
        }
    }

    onCreateSuccess(client)
    {
        let clientList = [...this.state.clientList];
        let oldClient = lodash.find(clientList, c => c.id === undefined);
        lodash.merge(oldClient, client);
        this.setState({
            providerList: clientList,
            newProvider: oldClient,
            deletedClientId: undefined
        })
    }

    onUpdateSuccess(client)
    {
        let clientList = [...this.state.clientList];
        let oldClient = lodash.find(clientList, p => p.id === client.id);
        lodash.merge(oldClient, client);
        this.setState({
            clientList: clientList,
            newProxy: undefined,
            deletedClientId: undefined
        })
    }

    removeClient(id)
    {
        let clientList = [...this.state.clientList];
        let oldClient = clientList.filter(client => client.id === id)[0];
        lodash.remove(clientList, client => client.id === id);
        this.setState({
            clientList: clientList,
            newClient: undefined,
            deletedClientId: oldClient.clientId,
            errors: {}
        })
    }

    render()
    {
        return (
            <React.Fragment>
                <LinkContainer exact
                               to={"/openIdProvider/"}>
                    <a href={"/#"}>
                        <h5 style={{height: "35px", padding: "0", paddingLeft: "10px"}}>
                            <ArrowLeftCircle style={{color: "bisque"}} height={"35px"} size={"25px"} />
                            <span style={{marginLeft: "15px"}}>Back to Provider Overview</span>
                        </h5>
                    </a>
                </LinkContainer>

                <h5>Provider: <span style={{color: "lightgray"}}>{this.state.provider.name}</span></h5>

                <p className={"add-new-resource"} onClick={this.addNewClient}>
                    <span className={"add-new-resource"}>Add new Client <br /><FileEarmarkPlus /></span>
                </p>
                <h2>
                    <span>OpenID Client List</span>
                </h2>
                <Alert id={"save-alert-success"} variant={"success"}
                       show={new Optional(this.state.newClient).isPresent()}>
                    <Form.Text><GoThumbsup /> Client with clientId
                                              '{new Optional(this.state.newClient).map(client => client.clientId)
                                                                                  .orElse("-")}'
                                              was successfully created</Form.Text>
                </Alert>
                <Alert id="card-list-deletion-success"
                       variant={"success"}
                       show={new Optional(this.state.deletedClientId).isPresent()}>
                    <Form.Text>
                        <GoThumbsup /> OpenID Client "{this.state.deletedClientId}" was successfully deleted
                    </Form.Text>
                </Alert>
                <ErrorMessagesAlert errors={this.state.errors} />
                <CardDeck>
                    {
                        this.state.clientList.map((client) =>
                        {
                            return <OpenIdClientCardEntry key={new Optional(client.id).orElse("new")}
                                                          provider={this.state.provider}
                                                          scimResourcePath={this.scimResourcePath}
                                                          client={client}
                                                          keyInfos={this.state.keyInfos}
                                                          onCreateSuccess={this.onCreateSuccess}
                                                          onUpdateSuccess={this.onUpdateSuccess}
                                                          onDeleteSuccess={this.removeClient} />
                        })
                    }
                </CardDeck>
            </React.Fragment>
        )
    }
}

class OpenIdClientCardEntry extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {
            showModal: false,
            editMode: new Optional(props.client).map(val => val.id).map(val => false).orElse(true),
            authenticationType: props.client.authenticationType,
            client: JSON.parse(JSON.stringify(props.client))
        }
        this.setState = this.setState.bind(this);
        this.resetEditMode = this.resetEditMode.bind(this);
        this.scimClient = new ScimClient(this.props.scimResourcePath, this.setState);
        this.formReference = createRef();

        this.scimComponentBasics = new ScimComponentBasics({
            scimClient: this.scimClient,
            formReference: this.formReference,
            getOriginalResource: () => this.props.client,
            getCurrentResource: () => this.state.client,
            setCurrentResource: resource => this.setState({client: resource}),
            setState: this.setState,
            onCreateSuccess: this.props.onCreateSuccess,
            onUpdateSuccess: this.props.onUpdateSuccess,
            onDeleteSuccess: this.props.onDeleteSuccess
        });
    }

    async resetEditMode()
    {
        this.scimComponentBasics.resetEditMode();
        let client = JSON.parse(JSON.stringify(this.props.client));
        await this.setState({client: client, authenticationType: client.authenticationType});
    }

    render()
    {
        let aliases = [];
        this.props.keyInfos.forEach(keyInfo =>
        {
            if (keyInfo.hasPrivateKey === true)
            {
                aliases.push(keyInfo.alias);
            }
        });
        return (
            <Card id={"client-card-" + this.state.client.id} key={this.state.client.id}
                  border={"warning"} bg={"dark"} className={"resource-card client-card"}>
                <Form onSubmit={this.scimComponentBasics.onSubmit} ref={this.formReference}>

                    <Modal id={"delete-dialog-" + this.state.client.id}
                           show={this.state.showModal}
                           variant="danger"
                           title={"Delete OpenID Client with clientId '" + this.state.client.clientId + "'"}
                           message="Are you sure?"
                           submitButtonText="delete"
                           onSubmit={this.scimComponentBasics.deleteResource}
                           cancelButtonText="cancel"
                           onCancel={() => this.scimComponentBasics.setStateValue("showModal", false)}>
                    </Modal>

                    <Alert id={"save-alert-success"} variant={"success"}
                           show={new Optional(this.state.success).orElse(false)}>
                        <Form.Text><GoThumbsup /> OpenID Client was successfully updated</Form.Text>
                    </Alert>

                    <ErrorMessagesAlert errors={this.state.errors} />

                    <Card.Header id={"client-card-header-" + this.state.client.id}>
                        <div className={"card-name-header"}>
                            <div className={"card-name-header"}>
                                {
                                    this.state.client.clientId !== undefined &&
                                    <LinkContainer exact
                                                   to={"/openIdProvider/" + this.props.provider.id + "/client/"
                                                       + this.state.client.id}>
                                        <a href={"/#"}>
                                            <h5>
                                                <ArrowRightCircle style={{color: "bisque", marginRight: "15px"}}
                                                                  size={"20px"} height={"30px"} />
                                                Client '{this.state.client.clientId}'
                                            </h5>
                                        </a>
                                    </LinkContainer>
                                }
                            </div>
                        </div>
                        <CardControlIcons resource={this.state.client}
                                          spinner={<LoadingSpinner show={this.state.showSpinner} />}
                                          editMode={this.state.editMode}
                                          createResource={this.scimComponentBasics.createResource}
                                          updateResource={this.scimComponentBasics.updateResource}
                                          resetEditMode={this.resetEditMode}
                                          edit={() => this.scimComponentBasics.setStateValue("editMode", true)}
                                          showModal={() => this.scimComponentBasics.setStateValue("showModal", true)} />
                        {/* this button enables pressing enter in the edit form */}
                        <Button id={"upload"} type="submit" hidden={true} />
                    </Card.Header>

                    <Card.Body>
                        <React.Fragment>
                            <Table size="sm" variant="dark" borderless striped>
                                <tbody>
                                    <HiddenCardEntry name="openIdProviderId"
                                                     type="number"
                                                     value={this.props.provider.id}
                                                     onError={fieldName => this.scimClient.getErrors(this.state,
                                                         fieldName)} />
                                    <ModifiableCardEntry header={"Client ID"}
                                                         name={"clientId"}
                                                         resourceId={this.state.client.id}
                                                         editMode={this.state.editMode}
                                                         resourceValue={this.state.client.clientId}
                                                         placeholder={"The identifier for this client at its OpenID Provider"}
                                                         onChange={this.scimComponentBasics.updateInput}
                                                         onError={fieldName => this.scimClient.getErrors(
                                                             this.state, fieldName)} />
                                    <CardRadioSelector header={"Authentication Type"}
                                                       name={"authenticationType"}
                                                       editMode={this.state.editMode}
                                                       selections={["basic", "jwt"]}
                                                       selected={new Optional(this.state.authenticationType).orElse(
                                                           "basic")}
                                                       onChange={e =>
                                                       {
                                                           this.scimComponentBasics.updateInput(e.target.name,
                                                               e.target.value);
                                                           this.setState({authenticationType: e.target.value})
                                                       }}
                                                       onError={fieldName => this.scimClient.getErrors(this.state,
                                                           fieldName)}
                                    />
                                    {
                                        this.state.authenticationType === "basic" &&
                                        <ModifiableCardEntry header={"Client Secret"}
                                                             name={"clientSecret"}
                                                             resourceId={this.state.client.id}
                                                             editMode={this.state.editMode}
                                                             resourceValue={this.state.client.clientSecret}
                                                             placeholder={"The password to access the OpenID Providers token endpoint"}
                                                             onChange={this.scimComponentBasics.updateInput}
                                                             onError={fieldName => this.scimClient.getErrors(
                                                                 this.state, fieldName)} />

                                    }
                                    {
                                        this.state.authenticationType === "jwt" &&
                                        <React.Fragment>
                                            <CardListSelector header={"Signature Key Reference"}
                                                              name={"signingKeyRef"}
                                                              editMode={this.state.editMode}
                                                              selections={["", ...aliases]}
                                                              selected={this.state.client.signingKeyRef}
                                                              onChange={e => this.scimComponentBasics.updateInput(
                                                                  e.target.name, e.target.value)}
                                                              onError={fieldName => this.scimClient.getErrors(
                                                                  this.state, fieldName)}

                                            />
                                            <ApplicationInfoContext.Consumer>
                                                {appInfo =>
                                                    <CardListSelector header={"JWT Signature Algorithm"}
                                                                      name={"signatureAlgorithm"}
                                                                      editMode={this.state.editMode}
                                                                      selections={["",
                                                                                   ...appInfo.jwtInfo.signatureAlgorithms]}
                                                                      selected={this.state.client.signatureAlgorithm}
                                                                      onChange={e => this.scimComponentBasics.updateInput(
                                                                          e.target.name, e.target.value)}
                                                                      onError={fieldName => this.scimClient.getErrors(
                                                                          this.state, fieldName)}

                                                    />
                                                }
                                            </ApplicationInfoContext.Consumer>
                                            <ModifiableCardEntry header={"Audience"}
                                                                 name={"audience"}
                                                                 resourceId={this.state.client.audience}
                                                                 editMode={this.state.editMode}
                                                                 resourceValue={this.state.client.audience}
                                                                 placeholder={"audience value that should match the providers issuer-value"}
                                                                 onChange={this.scimComponentBasics.updateInput}
                                                                 onError={fieldName => this.scimClient.getErrors(
                                                                     this.state, fieldName)} />
                                            <CardListSelector header={"Decryption Key Reference"}
                                                              name={"decryptionKeyRef"}
                                                              editMode={this.state.editMode}
                                                              selections={["", ...aliases]}
                                                              selected={this.state.client.decryptionKeyRef}
                                                              onChange={e => this.scimComponentBasics.updateInput(
                                                                  e.target.name, e.target.value)}

                                            />
                                        </React.Fragment>
                                    }
                                    <CardDateRows resource={this.state.client} />
                                </tbody>
                            </Table>
                        </React.Fragment>
                    </Card.Body>
                </Form>
            </Card>
        );
    }
}