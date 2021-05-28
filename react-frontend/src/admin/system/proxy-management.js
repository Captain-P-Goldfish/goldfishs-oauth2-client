import React, {createRef} from "react";
import ScimClient from "../../scim/scim-client";
import Modal from "../../base/modal";
import {Alert, Card, CardDeck, Table} from "react-bootstrap";
import {FileEarmarkPlus} from "react-bootstrap-icons";
import {Optional} from "../../services/utils";
import Form from "react-bootstrap/Form";
import {GoThumbsup} from "react-icons/go";
import {
    CardControlIcons,
    CardDateRows,
    ErrorMessagesAlert,
    LoadingSpinner,
    ModifiableCardEntry
} from "../../base/form-base";
import * as lodash from "lodash";
import Button from "react-bootstrap/Button";
import ScimComponentBasics from "../../scim/scim-component-basics";


export default class ProxyManagement extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {
            proxyList: [],
            showSpinner: false
        }
        this.scimResourcePath = "/scim/v2/Proxy";
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient(this.scimResourcePath, this.setState);
        this.addNewProxy = this.addNewProxy.bind(this);
        this.onUpdateSuccess = this.onUpdateSuccess.bind(this);
        this.onCreateSuccess = this.onCreateSuccess.bind(this);
        this.addProxy = this.addProxy.bind(this);
        this.removeProxy = this.removeProxy.bind(this);
    }

    async componentDidMount()
    {
        let startIndex = new Optional((this.state.currentPage * window.MAX_RESULTS) + 1).filter(val => !isNaN(val))
                                                                                        .orElse(1);
        let count = window.MAX_RESULTS;

        await this.scimClient.listResources({
            startIndex: startIndex,
            count: count,
            sortBy: 'id'
        }).then(listResponse =>
        {
            listResponse.resource.then(listResponse =>
            {
                this.setState({
                    proxyList: new Optional(listResponse.Resources).orElse([]),
                    errors: {},
                    newProxy: undefined,
                    deletedProxyId: undefined
                })
            })
        });
    }

    addNewProxy()
    {
        let proxyList = [...this.state.proxyList];
        const resource = proxyList.filter(proxy => proxy.id === undefined);
        if (resource.length === 0)
        {
            proxyList.unshift({});
            this.setState({
                proxyList: proxyList,
                newProxy: undefined,
                deletedProxyId: undefined
            })
        }
        else
        {
            this.setState({
                errors: {
                    errorMessages: ["There is already a new form available in the view."]
                },
                newProxy: undefined,
                deletedProxyId: undefined
            })
        }
    }

    onUpdateSuccess(proxy)
    {
        let proxyList = [...this.state.proxyList];
        let oldProxy = lodash.find(proxyList, p => p.id === proxy.id);
        lodash.merge(oldProxy, proxy);
        this.setState({
            proxyList: proxyList,
            newProxy: undefined,
            deletedProxyId: undefined
        })
    }

    onCreateSuccess(proxy)
    {
        let proxyList = [...this.state.proxyList];
        let oldProxy = lodash.find(proxyList, p => p.id === undefined);
        lodash.merge(oldProxy, proxy);
        this.setState({
            proxyList: proxyList,
            newProxy: oldProxy,
            deletedProxyId: undefined
        })
    }

    addProxy(proxy)
    {
        let proxyList = [...this.state.proxyList];
        proxyList.unshift(proxy);

        this.setState({
            proxyList: proxyList,
            newProxy: undefined,
            deletedProxyId: undefined
        })
    }

    removeProxy(id)
    {
        let proxyList = [...this.state.proxyList];
        const newProxyList = proxyList.filter(proxy => proxy.id !== id)
        this.setState({
            proxyList: newProxyList,
            newProxy: undefined,
            deletedProxyId: id,
            errors: {}
        })
    }

    render()
    {
        return (
            <React.Fragment>
                <p className={"add-new-resource"} onClick={this.addNewProxy}>
                    <span className={"add-new-resource"}>Add new Proxy <br /><FileEarmarkPlus /></span>
                </p>
                <h2>Proxies</h2>
                <Alert id={"save-alert-success"} variant={"success"}
                       show={new Optional(this.state.newProxy).isPresent()}>
                    <Form.Text><GoThumbsup /> Proxy with id
                                              '{new Optional(this.state.newProxy).map(proxy => proxy.id)
                                                                                 .orElse(-1)}'
                                              was successfully created</Form.Text>
                </Alert>
                <Alert id="card-list-deletion-success"
                       variant={"success"}
                       show={new Optional(this.state.deletedProxyId).isPresent()}>
                    <Form.Text>
                        <GoThumbsup /> Proxy with ID "{this.state.deletedProxyId}" was successfully deleted
                    </Form.Text>
                </Alert>
                <ErrorMessagesAlert errors={this.state.errors} />
                <CardDeck>
                    {
                        this.state.proxyList.map((proxy) =>
                        {
                            return <ProxyCardEntry key={new Optional(proxy.id).orElse("new")}
                                                   scimResourcePath={this.scimResourcePath}
                                                   proxy={proxy}
                                                   onCreateSuccess={this.onCreateSuccess}
                                                   onUpdateSuccess={this.onUpdateSuccess}
                                                   onDeleteSuccess={this.removeProxy} />
                        })
                    }
                </CardDeck>
            </React.Fragment>
        );
    }

}

class ProxyCardEntry extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {
            showModal: false,
            editMode: new Optional(props.proxy).map(val => val.id).map(val => false).orElse(true),
            proxy: JSON.parse(JSON.stringify(props.proxy))
        }
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient(this.props.scimResourcePath, this.setState);
        this.formReference = createRef();

        this.scimComponentBasics = new ScimComponentBasics({
            scimClient: this.scimClient,
            formReference: this.formReference,
            getOriginalResource: () => this.props.proxy,
            getCurrentResource: () => this.state.proxy,
            setCurrentResource: resource => this.setState({proxy: resource}),
            setState: this.setState,
            onCreateSuccess: this.props.onCreateSuccess,
            onUpdateSuccess: this.props.onUpdateSuccess,
            onDeleteSuccess: this.props.onDeleteSuccess
        });
    }

    render()
    {
        return (
            <Card id={"proxy-card-" + this.state.proxy.id} key={this.state.proxy.id}
                  border={"warning"} bg={"dark"} className={"resource-card provider-card"}>
                <Form onSubmit={this.scimComponentBasics.onSubmit} ref={this.formReference}>

                    <Modal id={"delete-dialog-" + this.state.proxy.id}
                           show={this.state.showModal}
                           variant="danger"
                           title={"Delete Proxy with ID '" + this.state.proxy.id + "'"}
                           message="Are you sure?"
                           submitButtonText="delete"
                           onSubmit={this.scimComponentBasics.deleteResource}
                           cancelButtonText="cancel"
                           onCancel={() => this.scimComponentBasics.setStateValue("showModal", false)}>
                    </Modal>

                    <Alert id={"save-alert-success-" + this.state.proxy.id} variant={"success"}
                           show={new Optional(this.state.success).orElse(false)}>
                        <Form.Text><GoThumbsup /> Proxy was successfully updated</Form.Text>
                    </Alert>

                    <ErrorMessagesAlert errors={this.state.errors} />

                    <Card.Header id={"proxy-card-header-" + this.state.proxy.id}>
                        <div className={"card-name-header"}>
                            {
                                this.state.proxy.id !== undefined &&
                                <h5>Proxy '{this.state.proxy.id}'</h5>
                            }
                        </div>
                        <CardControlIcons resource={this.state.proxy}
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
                                    <ModifiableCardEntry header={"Hostname"}
                                                         name={"hostname"}
                                                         resourceId={this.state.proxy.id}
                                                         editMode={this.state.editMode}
                                                         resourceValue={this.state.proxy.hostname}
                                                         placeholder={"The URL to the OpenID Providers discovery endpoint"}
                                                         onChange={this.scimComponentBasics.updateInput}
                                                         onError={fieldName => this.scimClient.getErrors(
                                                             this.state, fieldName)} />
                                    <ModifiableCardEntry header={"Port"}
                                                         name={"port"}
                                                         type={"number"}
                                                         resourceId={this.state.proxy.id}
                                                         editMode={this.state.editMode}
                                                         resourceValue={this.state.proxy.port}
                                                         placeholder={"The port number of the Proxy"}
                                                         onChange={this.scimComponentBasics.updateInput}
                                                         onError={fieldName => this.scimClient.getErrors(
                                                             this.state, fieldName)} />
                                    <ModifiableCardEntry header={"Username"}
                                                         name={"username"}
                                                         resourceId={this.state.proxy.id}
                                                         editMode={this.state.editMode}
                                                         resourceValue={this.state.proxy.username}
                                                         placeholder={"The username to authenticate at the proxy"}
                                                         onChange={this.scimComponentBasics.updateInput}
                                                         onError={fieldName => this.scimClient.getErrors(
                                                             this.state, fieldName)} />
                                    <ModifiableCardEntry header={"Password"}
                                                         name={"password"}
                                                         resourceId={this.state.proxy.id}
                                                         editMode={this.state.editMode}
                                                         resourceValue={this.state.proxy.password}
                                                         placeholder={"The password to authenticate at the Proxy"}
                                                         onChange={this.scimComponentBasics.updateInput}
                                                         onError={fieldName => this.scimClient.getErrors(
                                                             this.state, fieldName)} />
                                    <CardDateRows resource={this.state.proxy} />
                                </tbody>
                            </Table>
                        </React.Fragment>
                    </Card.Body>
                </Form>
            </Card>
        );
    }
}
