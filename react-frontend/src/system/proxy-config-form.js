import React from "react";
import ScimClient from "../scim/scim-client";
import Modal from "../base/modal";
import {Alert, Card, CardDeck, Table} from "react-bootstrap";
import {FileEarmarkPlus} from "react-bootstrap-icons";
import Spinner from "react-bootstrap/Spinner";
import {Optional} from "../services/utils";
import Form from "react-bootstrap/Form";
import {GoThumbsup} from "react-icons/go";
import ScimComponent, {CardControlIcons, CardDateRows} from "../scim/scim-component";
import {ErrorMessageList} from "../base/config-page-form";
import {CardInputField} from "../base/card-base";


class ProxyCardEntry extends ScimComponent
{
    constructor(props)
    {
        super(props);
        this.state = {
            showModal: false,
            editMode: new Optional(props.proxy).map(val => val.id).map(val => false).orElse(true),
            resource: props.proxy
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
            <React.Fragment>
                <Card id={"proxy-card-" + this.state.resource.id} key={this.state.resource.id}
                      border={"warning"} bg={"dark"} className={"resource-card provider-card"}>
                    <Alert id={this.formId + "-alert-error"} variant={"danger"}
                           show={errorMessages.length !== 0}>
                        <ErrorMessageList fieldErrors={errorMessages} backgroundClass={""} />
                    </Alert>
                    <Modal id={"delete-dialog-" + this.state.resource.id}
                           show={this.state.showModal}
                           variant="danger"
                           title={"Delete Proxy with ID '" + this.state.resource.id + "'"}
                           message="Are you sure?"
                           submitButtonText="delete"
                           onSubmit={() => this.deleteEntry(this.state.resource.id)}
                           cancelButtonText="cancel"
                           onCancel={this.hideModal}>
                    </Modal>
                    <Card.Header id={"proxy-card-header-" + this.state.resource.id}>
                        <div className={"card-name-header"}>
                            {
                                this.state.resource.id !== undefined &&
                                <h5>Proxy '{this.state.resource.id}'</h5>
                            }
                        </div>
                        <CardControlIcons resource={this.state.resource}
                                          spinner={spinner}
                                          editMode={this.state.editMode}
                                          createResource={this.createResource}
                                          updateResource={this.updateResource}
                                          resetEditMode={this.resetEditMode}
                                          edit={this.edit}
                                          showModal={this.showModal} />
                    </Card.Header>
                    <Card.Body>
                        <React.Fragment>
                            <Table size="sm" variant="dark" borderless striped>
                                <tbody>
                                    <tr>
                                        <th>Hostname</th>
                                        <td id={"proxy-card-" + this.props.proxy.id + "-hostname"}>
                                            {
                                                this.state.editMode &&
                                                <CardInputField value={this.props.proxy.hostname}
                                                                name={"hostname"}
                                                                placeHolder={"The Hostname or IP of the proxy"}
                                                                onChange={this.onChange}
                                                                onError={this.getErrors} />
                                            }
                                            {
                                                !this.state.editMode &&
                                                this.props.proxy.hostname
                                            }
                                        </td>
                                    </tr>
                                    <tr>
                                        <th>Port</th>
                                        <td id={"proxy-card-" + this.props.proxy.id + "-port"}>
                                            {
                                                this.state.editMode &&
                                                <CardInputField value={this.props.proxy.port}
                                                                type={"number"}
                                                                name={"port"}
                                                                placeHolder={"The port number of the Proxy"}
                                                                onChange={this.onChange}
                                                                onError={this.getErrors} />
                                            }
                                            {
                                                !this.state.editMode &&
                                                this.props.proxy.port
                                            }
                                        </td>
                                    </tr>
                                    <tr>
                                        <th>Username</th>
                                        <td id={"proxy-card-" + this.props.proxy.id + "-username"}>
                                            {
                                                this.state.editMode &&
                                                <CardInputField value={this.props.proxy.username}
                                                                name={"username"}
                                                                placeHolder={"The username to authenticate at the proxy"}
                                                                onChange={this.onChange}
                                                                onError={this.getErrors} />
                                            }
                                            {
                                                !this.state.editMode &&
                                                this.props.proxy.username
                                            }
                                        </td>
                                    </tr>
                                    <tr>
                                        <th>Password</th>
                                        <td id={"proxy-card-" + this.props.proxy.id + "-password"}>
                                            {
                                                this.state.editMode &&
                                                <CardInputField value={this.props.proxy.password}
                                                                name={"password"}
                                                                placeHolder={"The password to authenticate at the Proxy"}
                                                                onChange={this.onChange}
                                                                onError={this.getErrors} />
                                            }
                                            {
                                                !this.state.editMode &&
                                                this.props.proxy.password
                                            }
                                        </td>
                                    </tr>
                                    <CardDateRows resource={this.state.resource} />
                                </tbody>
                            </Table>
                        </React.Fragment>
                    </Card.Body>
                </Card>

            </React.Fragment>
        );
    }
}

export default class ProxyList extends React.Component
{
    constructor(props)
    {
        super(props);
        this.scimClient = new ScimClient("/scim/v2/Proxy");
        this.state = {
            errors: {},
            proxyList: [],
            currentPage: 0
        };
        this.errorListener = this.errorListener.bind(this);
        this.scimClient.setErrorListener(this.errorListener);
        this.removeProxy = this.removeProxy.bind(this);
        this.addNewProxy = this.addNewProxy.bind(this);
        this.onCreateSuccess = this.onCreateSuccess.bind(this);
        this.onUpdateSuccess = this.onUpdateSuccess.bind(this);
    }

    errorListener(errorsObject)
    {
        this.setState({
            errors: new Optional(errorsObject).orElse({}),
            deletedProxyId: undefined
        })
    }

    async componentDidMount()
    {
        let startIndex = (this.state.currentPage * window.MAX_RESULTS) + 1;
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
                    deletedProxyId: undefined
                })
            })
        });
    }

    addNewProxy()
    {
        let proxyList = this.state.proxyList;
        const resource = proxyList.filter(proxy => proxy.id === undefined);
        if (resource.length === 0)
        {
            proxyList.unshift({});
            this.setState({
                proxyList: proxyList,
                deletedProxyId: undefined
            })
        }
        else
        {
            this.setState({
                errors: {
                    errorMessages: ["There is already a new form available in the view."]
                },
                deletedProxyId: undefined
            })
        }
    }

    onUpdateSuccess(proxy)
    {
        this.removeProxy(proxy.id);
        this.addProxy(proxy);
    }

    onCreateSuccess(proxy)
    {
        this.removeProxy(undefined);
        this.addProxy(proxy);
    }

    addProxy(proxy)
    {
        let proxyList = this.state.proxyList;
        proxyList.unshift(proxy);

        this.setState({
            proxyList: proxyList,
            deletedProxyId: undefined
        })
    }

    removeProxy(id)
    {
        let proxyList = this.state.proxyList
        const newProxyList = proxyList.filter(proxy => proxy.id !== id)
        this.setState({
            proxyList: newProxyList,
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
                <Alert id="card-list-deletion-success"
                       variant={"success"}
                       show={this.state.deletedProxyId !== undefined}>
                    <Form.Text>
                        <GoThumbsup /> Proxy with ID "{this.state.deletedProxyId}" was successfully deleted
                    </Form.Text>
                </Alert>
                <Alert id={this.props.formId + "-alert-error"} variant={"danger"}
                       show={new Optional(this.state.errors).map(val => val.errorMessages).map(val => true).orElse(
                           false)}>
                    <ErrorMessageList fieldErrors={this.state.errors.errorMessages} backgroundClass={""} />
                </Alert>
                <CardDeck>
                    {
                        this.state.proxyList.map((proxy) =>
                        {
                            return <ProxyCardEntry key={new Optional(proxy.id).orElse(-1)}
                                                   scimResourcePath={"/scim/v2/Proxy"}
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