import React from "react";
import {FormInputField} from "../base/config-page-form";
import ScimConfigPageForm from "../base/scim-config-page-form";
import ScimClient from "../scim/scim-client";
import Modal from "../base/modal";
import {Alert, Card, CardDeck, Table} from "react-bootstrap";
import {InfoCircle, PencilSquare, TrashFill} from "react-bootstrap-icons";
import Spinner from "react-bootstrap/Spinner";
import {Optional} from "../services/utils";
import Form from "react-bootstrap/Form";
import {GoThumbsup} from "react-icons/go";
import Button from "react-bootstrap/Button";


class ProxyCardEntry extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {showModal: false}
        this.deleteEntry = this.deleteEntry.bind(this);
        this.edit = this.edit.bind(this);
        this.showModal = this.showModal.bind(this);
        this.hideModal = this.hideModal.bind(this);
    }

    async deleteEntry()
    {
        let scimClient = new ScimClient("/scim/v2/Proxy");
        let response = await scimClient.deleteResource(this.props.proxy.id);
        if (response.success)
        {
            this.props.onDeleteSuccess(this.props.proxy);
        }
    }

    edit()
    {
        this.props.update(this.props.proxy);
    }

    showModal()
    {
        this.setState({showModal: true})
    }

    hideModal()
    {
        this.setState({showModal: false})
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

        return (
            <React.Fragment>
                <Card id={"proxy-card-" + this.props.proxy.id} key={this.props.proxy.id}
                      border={"warning"} bg={"dark"} className={"resource-card"}>
                    <Modal id={"delete-dialog-" + this.props.proxy.id}
                           show={this.state.showModal}
                           variant="danger"
                           title={"Delete Proxy '" + this.props.proxy.id + "'"}
                           message="Are you sure?"
                           submitButtonText="delete"
                           onSubmit={this.deleteEntry}
                           cancelButtonText="cancel"
                           onCancel={this.hideModal}>
                    </Modal>
                    <Card.Header id={"proxy-card-header-" + this.props.proxy.id}>
                        Proxy {this.props.proxy.id}
                        <div className="card-delete-icon">
                            {spinner}
                            <PencilSquare title={"edit"} id={"update-icon-" + this.props.proxy.id}
                                          onClick={this.edit} style={{marginRight: 5 + 'px'}} />
                            <TrashFill title={"delete"} id={"delete-icon-" + this.props.proxy.id}
                                       onClick={this.showModal} />
                        </div>
                    </Card.Header>
                    <Card.Body>
                        <React.Fragment>
                            <Table size="sm" variant="dark" borderless striped>
                                <tbody>
                                    <tr>
                                        <th>Hostname</th>
                                        <td id={"proxy-card-" + this.props.proxy.id + "-hostname"}>
                                            {this.props.proxy.hostname}
                                        </td>
                                    </tr>
                                    <tr>
                                        <th>Port</th>
                                        <td id={"proxy-card-" + this.props.proxy.id + "-port"}>
                                            {this.props.proxy.port}
                                        </td>
                                    </tr>
                                    <tr>
                                        <th>Username</th>
                                        <td id={"proxy-card-" + this.props.proxy.id + "-username"}>
                                            {this.props.proxy.username}
                                        </td>
                                    </tr>
                                    <tr>
                                        <th>Password</th>
                                        <td id={"proxy-card-" + this.props.proxy.id + "-password"}>
                                            {this.props.proxy.password}
                                        </td>
                                    </tr>
                                    <tr>
                                        <th>Created</th>
                                        <td>{new Date(this.props.proxy.meta.created).toUTCString()}</td>
                                    </tr>
                                    <tr>
                                        <th>LastModified</th>
                                        <td>{new Date(this.props.proxy.meta.lastModified).toUTCString()}</td>
                                    </tr>
                                </tbody>
                            </Table>
                        </React.Fragment>
                    </Card.Body>
                </Card>
            </React.Fragment>
        );
    }
}

class ProxyList extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {proxies: new Optional(this.props.proxies).orElse([])}
        this.removeProxy = this.removeProxy.bind(this);
    }

    componentDidUpdate(prevProps, prevState, snapshot)
    {
        if (this.props !== prevProps)
        {
            this.setState({
                proxies: new Optional(this.props.proxies).orElse([]),
                deletedProxy: undefined
            })
        }
    }

    removeProxy(proxy)
    {
        let proxyList = this.state.proxies;
        const index = proxyList.indexOf(proxy);
        proxyList.splice(index, 1)
        this.setState({
            proxies: proxyList,
            deletedProxy: proxy
        })
    }

    render()
    {
        return (
            <React.Fragment>
                <h2>Proxies</h2>
                <Alert id="card-list-deletion-success"
                       variant={"success"}
                       show={this.state.deletedProxy !== undefined}>
                    <Form.Text>
                        <GoThumbsup /> Proxy with ID
                                       "{new Optional(this.state.deletedProxy).map(val => val.id).orElse("")}"
                                       was successfully deleted
                    </Form.Text>
                </Alert>
                <CardDeck>
                    {
                        this.state.proxies.map((proxy) =>
                        {
                            return <ProxyCardEntry key={proxy.id}
                                                   proxy={proxy}
                                                   update={this.props.update}
                                                   onDeleteSuccess={this.removeProxy} />
                        })
                    }
                </CardDeck>
            </React.Fragment>
        );
    }

}

export default class ProxyConfigForm extends React.Component
{

    constructor(props)
    {
        super(props);
        this.state = {
            proxies: [],
            updateProxy: {}
        };
        this.handleSaveSuccess = this.handleSaveSuccess.bind(this);
        this.toUpdateMode = this.toUpdateMode.bind(this);
        this.cancelUpdateMode = this.cancelUpdateMode.bind(this);
        this.onFieldChange = this.onFieldChange.bind(this);
    }

    async componentDidMount()
    {
        let scimClient = new ScimClient("/scim/v2/Proxy");
        let response = await scimClient.listResources();
        response.resource.then(jsonResponse =>
        {
            this.setState({
                proxies: new Optional(jsonResponse.Resources).orElse([])
            })
        })
    }

    handleSaveSuccess(status, response)
    {
        let proxyList = this.state.proxies.filter((item) => item.id !== response.id);
        proxyList.push(response);
        proxyList.sort((a, b) => a.id < b.id ? -1 : 1)
        this.setState({
            proxies: proxyList,
            updateProxy: {}
        });
    }

    toUpdateMode(proxy)
    {
        this.setState({
            updateProxy: proxy,
            originalProxy: JSON.parse(JSON.stringify(proxy))
        })
    }

    cancelUpdateMode()
    {
        let originalProxy = this.state.originalProxy;
        let proxyList = this.state.proxies.filter((item) => item.id !== originalProxy.id);
        proxyList.push(originalProxy);
        proxyList.sort((a, b) => a.id < b.id ? -1 : 1)

        this.setState({
            proxies: proxyList,
            updateProxy: {}
        })
    }

    onFieldChange(name, value, onChange)
    {
        onChange(name, value);
        let updateProxy = this.state.updateProxy;
        updateProxy[name] = value
        this.setState({
            updateProxy: updateProxy
        });
    }

    render()
    {
        let isUpdateActive = this.state.updateProxy.id !== undefined;
        let additionalButtons = (
            isUpdateActive &&
            <Button id={"cancel"} key={"cancel"} type="cancel" onClick={this.cancelUpdateMode}
                    style={{marginLeft: 5 + 'px'}}>
                cancel
            </Button>
        );
        let submitUrl = "/scim/v2/Proxy" + (isUpdateActive ? "/" + this.state.updateProxy.id : "");

        return (
            <React.Fragment>
                <ScimConfigPageForm formId="proxyForm"
                                    header="Proxy Settings"
                                    httpMethod={isUpdateActive ? "PUT" : "POST"}
                                    submitUrl={submitUrl}
                                    onSubmitSuccess={this.handleSaveSuccess}
                                    buttonId="submitButton"
                                    buttonText={isUpdateActive ? "update" : "save"}
                                    successMessage="Proxy was successfully saved"
                                    additionalButtons={[additionalButtons]}>
                    {({onChange, onError}) => (
                        <React.Fragment>
                            {
                                isUpdateActive &&
                                <React.Fragment>
                                    <Alert id="edit-mode-activated-alert"
                                           variant={"info"}>
                                        <Form.Text>
                                            <InfoCircle /> Updating proxy with id '{this.state.updateProxy.id}'
                                        </Form.Text>
                                    </Alert>
                                    <FormInputField name="Id"
                                                    label="id"
                                                    value={this.state.updateProxy.id}
                                                    readOnly={true}
                                                    onChange={onChange}
                                                    onError={onError} />
                                </React.Fragment>
                            }
                            <FormInputField name="hostname"
                                            label="Hostname"
                                            value={new Optional(this.state.updateProxy).map(val => val.hostname).orElse(
                                                "")}
                                            onChange={(name, value) => this.onFieldChange(name, value, onChange)}
                                            onError={onError} />
                            <FormInputField name="port"
                                            label="Port"
                                            type="number"
                                            value={new Optional(this.state.updateProxy).map(val => val.port).orElse("")}
                                            onChange={(name, value) => this.onFieldChange(name, value, onChange)}
                                            onError={onError} />
                            <FormInputField name="username"
                                            label="Username"
                                            value={new Optional(this.state.updateProxy).map(val => val.username).orElse(
                                                "")}
                                            onChange={(name, value) => this.onFieldChange(name, value, onChange)}
                                            onError={onError} />
                            <FormInputField name="password"
                                            label="Password"
                                            value={new Optional(this.state.updateProxy).map(val => val.password).orElse(
                                                "")}
                                            onChange={(name, value) => this.onFieldChange(name, value, onChange)}
                                            onError={onError} />
                        </React.Fragment>
                    )}
                </ScimConfigPageForm>

                <ProxyList proxies={this.state.proxies}
                           update={this.toUpdateMode} />
            </React.Fragment>
        )
    }
}