import React from "react";
import ConfigPageForm, {FormFileField, FormInputField, FormSelectField} from "../base/config-page-form";
import {Alert, Card, CardDeck} from "react-bootstrap";
import Modal from "../base/modal";
import Spinner from "react-bootstrap/Spinner";
import {InfoCircle, TrashFill} from "react-bootstrap-icons";
import {GoThumbsup} from "react-icons/go";
import Form from "react-bootstrap/Form";

class KeystoreAliasRepresentation extends React.Component {

    constructor(props) {
        super(props);
        this.state = {};
        this.onDeleteClick = this.onDeleteClick.bind(this);
        this.showModal = this.showModal.bind(this);
        this.hideModal = this.hideModal.bind(this);
    }

    onDeleteClick() {
        this.setState({showSpinner: true});
        let deleteUrl = "/keystore/delete-alias?alias=" + this.props.alias;
        fetch(deleteUrl, {
            method: "DELETE"
        })
            .then(response => {
                if (response.status === 204) {
                    this.props.onDeleteSuccess(this.props.alias);
                }
            })
    }

    showModal() {
        this.setState({showModal: true})
    }

    hideModal() {
        this.setState({showModal: false})
    }

    render() {
        let spinner;
        if (this.state.showSpinner) {
            spinner = <span style={{marginRight: 5 + 'px'}}>
                          <Spinner animation="border" variant="warning" size="sm" role="status" />
                      </span>;
        }

        return (
            <Card id={"alias-card-" + this.props.alias} border={"warning"} bg={"dark"} className={"alias-card"}>
                <Modal id={"delete-dialog-" + this.props.alias}
                       show={this.state.showModal}
                       variant="danger"
                       title={"Delete '" + this.props.alias + "'"}
                       message="Are you sure?"
                       submitButtonText="delete"
                       onSubmit={this.onDeleteClick}
                       cancelButtonText="cancel"
                       onCancel={this.hideModal}>
                </Modal>
                <Card.Header id={"alias-name-" + this.props.alias}>
                    {this.props.alias}
                    <div className="card-delete-icon">
                        {spinner}
                        <TrashFill id={"delete-button-" + this.props.alias} onClick={this.showModal}>
                        </TrashFill>
                    </div>
                </Card.Header>
                <Card.Body>
                    <Card.Subtitle>Issuer</Card.Subtitle>
                    <Card.Text id={"issuer-dn-" + this.props.alias}>
                        {this.props.certificateInfo.issuerDn}
                    </Card.Text>
                    <Card.Subtitle>Subject</Card.Subtitle>
                    <Card.Text id={"subject-dn-" + this.props.alias}>
                        {this.props.certificateInfo.subjectDn}
                    </Card.Text>
                    <Card.Subtitle>SHA-256 Fingerprint</Card.Subtitle>
                    <Card.Text id={"sha-256-" + this.props.alias}>
                        {this.props.certificateInfo.sha256fingerprint}
                    </Card.Text>
                    <Card.Subtitle>Valid From</Card.Subtitle>
                    <Card.Text id={"valid-from-" + this.props.alias}>
                        {this.props.certificateInfo.validFrom}
                    </Card.Text>
                    <Card.Subtitle>Valid Until</Card.Subtitle>
                    <Card.Text id={"valid-until-" + this.props.alias}>
                        {this.props.certificateInfo.validUntil}
                    </Card.Text>
                </Card.Body>
            </Card>
        )
    }
}

class KeystoreAliasList extends React.Component {

    constructor(props) {
        super(props);
        this.state = {};
        this.onDeleteSuccess = this.onDeleteSuccess.bind(this);
    }

    shouldComponentUpdate(nextProps, nextState, nextContext) {
        if (this.props.keystoreInfos.length !== nextProps.keystoreInfos.length) {
            this.setState({deletedAlias: undefined})
            return true;
        } else if (this.state.deletedAlias !== nextState.deletedAlias) {
            return true;
        }
        return false;
    }

    onDeleteSuccess(alias) {
        this.props.onDeleteSuccess(alias);
        this.setState({
            deletedAlias: alias
        });
    }

    render() {
        return (
            <React.Fragment>
                <h2>Current entries of the application keystore</h2>

                <Alert id="card-list-alert-no-entries"
                       variant={"warning"}
                       show={this.props.keystoreInfos.length === 0}>
                    <Form.Text>
                        <InfoCircle /> No Key entries added yet
                    </Form.Text>
                </Alert>

                <Alert id="card-list-deletion-success"
                       variant={"success"}
                       show={this.state.deletedAlias !== undefined}>
                    <Form.Text>
                        <GoThumbsup /> Alias "{this.state.deletedAlias}" was successfully deleted
                    </Form.Text>
                </Alert>
                {
                    this.props.keystoreInfos.length > 0
                    &&
                    <React.Fragment>
                        <CardDeck id="keystore-alias-entries">
                            {
                                this.props.keystoreInfos.map((keystoreInfo, index) => {
                                    return <KeystoreAliasRepresentation
                                        key={keystoreInfo.certificateInfo === undefined ? index : keystoreInfo.alias}
                                        alias={keystoreInfo.alias}
                                        certificateInfo={keystoreInfo.certificateInfo}
                                        onDeleteSuccess={this.onDeleteSuccess} />
                                })
                            }
                        </CardDeck>
                    </React.Fragment>
                }
            </React.Fragment>
        );
    }

}

export default class KeystoreConfigForm extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            stateId: "",
            keystoreInfos: [],
            aliasOptions: [],
            uploadFormDisabled: false,
            selectAliasFormDisabled: true
        }
        this.handleUploadSuccess = this.handleUploadSuccess.bind(this);
        this.handleSelectionSuccess = this.handleSelectionSuccess.bind(this);
        this.onAliasDeleteSuccess = this.onAliasDeleteSuccess.bind(this);
    }

    async componentDidMount() {
        fetch("/keystore/aliases")
            .then(response => {
                response.json().then(jsonResponse => {
                    jsonResponse.map(keystoreInfo => {
                        return this.handleSelectionSuccess(response.status, keystoreInfo);
                    })
                })
            })
    }

    handleUploadSuccess(status, response) {
        this.setState({
            stateId: response.stateId,
            aliasOptions: response.aliases,
            uploadFormDisabled: true,
            selectAliasFormDisabled: false
        });
    }

    handleSelectionSuccess(status, response) {
        let keystoreInfos = [];
        if (this.state.keystoreInfos !== undefined) {
            this.state.keystoreInfos.forEach(keystoreInfo => keystoreInfos.push(keystoreInfo));
        }
        keystoreInfos.push(response);
        this.setState({
            keystoreInfos: keystoreInfos
        });
    }

    onAliasDeleteSuccess(alias) {
        let keystoreInfos = this.state.keystoreInfos.filter(item => item.alias !== alias);
        this.setState({
            keystoreInfos: keystoreInfos
        });
    }

    render() {
        return (
            <React.Fragment>
                <ConfigPageForm formId="uploadForm"
                                header="Keystore Upload"
                                httpMethod="POST"
                                submitUrl="/keystore/upload"
                                onSubmitSuccess={this.handleUploadSuccess}
                                buttonId="uploadButton"
                                buttonText="Upload"
                                successMessage="Keystore was successfully uploaded"
                                disabled={this.state.uploadFormDisabled}>
                    {({onChange, onError}) => (
                        <React.Fragment>
                            <FormFileField name="keystoreFile"
                                           label="Keystore File"
                                           placeholder="Select a keystore file"
                                           onChange={onChange}
                                           onError={onError}
                            />
                            <FormInputField name="keystorePassword"
                                            label="Keystore Password"
                                            type="password"
                                            placeholder="Keystore Password"
                                            onChange={onChange}
                                            onError={onError} />
                        </React.Fragment>
                    )}
                </ConfigPageForm>
                <ConfigPageForm formId="aliasSelectionForm"
                                header="Alias Selection"
                                httpMethod="POST"
                                submitUrl="/keystore/select-alias"
                                onSubmitSuccess={this.handleSelectionSuccess}
                                buttonId="saveButton"
                                buttonText="Save"
                                successMessage="Key Entry was successfully added"
                                disabled={this.state.selectAliasFormDisabled}>
                    {({onChange, onError}) => (
                        <React.Fragment>
                            <FormInputField name="stateId"
                                            type="hidden"
                                            onChange={onChange}
                                            onError={onError}
                                            value={this.state.stateId} />
                            <FormSelectField name="aliases" label="Alias"
                                             onChange={onChange}
                                             onError={onError}
                                             options={this.state.aliasOptions} />
                            <FormInputField name="aliasOverride"
                                            label="Alias Override"
                                            type="text"
                                            placeholder="Optional value to override the alias on save"
                                            onChange={onChange}
                                            onError={onError} />
                            <FormInputField name="privateKeyPassword"
                                            label="Private Key Password"
                                            type="password"
                                            placeholder="Private Key Password"
                                            onChange={onChange}
                                            onError={onError} />
                        </React.Fragment>
                    )}
                </ConfigPageForm>

                <KeystoreAliasList keystoreInfos={this.state.keystoreInfos}
                                   onDeleteSuccess={this.onAliasDeleteSuccess} />
            </React.Fragment>
        );
    }
}

export {
    KeystoreAliasRepresentation
}