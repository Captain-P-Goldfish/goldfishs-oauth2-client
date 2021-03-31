import React from "react";
import FormBaseHoc from "../base/form-base-hoc";
import Modal from "../base/modal";
import {FormFilefield, FormInputfield, FormSelectfield} from "../formfields/formInputfield";
import {Alert, Card, CardDeck, Spinner} from "react-bootstrap";
import 'bootstrap/dist/css/bootstrap.min.css';
import {TrashFill} from "react-bootstrap-icons";
import Form from "react-bootstrap/Form";
import {GoThumbsup} from "react-icons/go";

class KeystoreUploadForm extends React.Component {

    render() {
        return (
            <React.Fragment>
                <FormFilefield name="keystoreFile" label="Keystore File" placeholder="Select a keystore file"
                               handleFieldChange={this.props.setFieldParam}
                               fieldErrors={this.props.fieldErrors} />
                <FormInputfield name="keystorePassword" label="Keystore Password" type="password"
                                placeholder="Keystore Password"
                                handleFieldChange={this.props.setFieldParam}
                                fieldErrors={this.props.fieldErrors} />
            </React.Fragment>
        );
    }
}

class KeystoreAliasForm extends React.Component {


    componentDidUpdate(prevProps, prevState, snapshot) {
        // this if-condition will only be true once which is when the keystore-form goes into its second state
        // (alias-selection)
        if (this.props.value !== prevProps.value) {
            // lifting stateId up to higher order component (FormBase)
            this.props.setFieldParam("stateId", this.props.value);
            // lifting the first alias entry up  higher order component (FormBase) since it is the currently selected
            this.props.setFieldParam("aliases", [this.props.aliases[0]]);
        }
    }

    render() {
        return (
            <React.Fragment>
                <FormInputfield name="stateId" type="hidden"
                                handleFieldChange={this.props.setFieldParam}
                                fieldErrors={this.props.fieldErrors}
                                value={this.props.value} />
                <FormSelectfield name="aliases" label="Alias"
                                 handleFieldChange={this.props.setFieldParam}
                                 fieldErrors={this.props.fieldErrors}
                                 options={this.props.aliases} />
                <FormInputfield name="aliasOverride" label="Alias Override"
                                type="text" placeholder="Optional value to override the alias on save"
                                handleFieldChange={this.props.setFieldParam}
                                fieldErrors={this.props.fieldErrors} />
                <FormInputfield name="privateKeyPassword" label="Private Key Password" type="password"
                                placeholder="Private Key Password"
                                handleFieldChange={this.props.setFieldParam}
                                fieldErrors={this.props.fieldErrors} />
            </React.Fragment>
        );
    }
}

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
        let deleteUrl = "/keystore/delete-alias?alias=" + this.props.keystoreInfo.alias;
        fetch(deleteUrl, {
            method: "DELETE"
        })
            .then(response => {
                if (response.status === 204) {
                    this.props.onDeleteSuccess(this.props.keystoreInfo.alias);
                }
                this.setState({showSpinner: false});
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

            <Card border={"warning"} bg={"dark"} className={"alias-card"}>
                <Modal show={this.state.showModal}
                       message="Are you sure?"
                       submitButtonText="delete"
                       onSubmit={this.onDeleteClick}
                       cancelButtonText="cancel"
                       onCancel={this.hideModal}>
                </Modal>
                <Card.Header>
                    {this.props.keystoreInfo.alias}
                    <div className="card-delete-icon">
                        {spinner}
                        <TrashFill className="card-delete-icon" onClick={this.showModal}>
                        </TrashFill>
                    </div>
                </Card.Header>
                <Card.Body>
                    <Card.Subtitle>Issuer</Card.Subtitle>
                    <Card.Text>
                        {this.props.keystoreInfo.certificateInfo.issuerDn}
                    </Card.Text>
                    <Card.Subtitle>Subject</Card.Subtitle>
                    <Card.Text>
                        {this.props.keystoreInfo.certificateInfo.subjectDn}
                    </Card.Text>
                    <Card.Subtitle>SHA-256 Fingerprint</Card.Subtitle>
                    <Card.Text>
                        {this.props.keystoreInfo.certificateInfo.sha256fingerprint}
                    </Card.Text>
                    <Card.Subtitle>Valid From</Card.Subtitle>
                    <Card.Text>
                        {this.props.keystoreInfo.certificateInfo.validFrom}
                    </Card.Text>
                    <Card.Subtitle>Valid Until</Card.Subtitle>
                    <Card.Text>
                        {this.props.keystoreInfo.certificateInfo.validUntil}
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

                <Alert variant={"success"} show={this.state.deletedAlias !== undefined}>
                    <Form.Text>
                        <GoThumbsup /> Alias "{this.state.deletedAlias}" was successfully deleted
                    </Form.Text>
                </Alert>

                <CardDeck>
                    {
                        this.props.keystoreInfos.map((keystoreInfo, index) => {
                            return <KeystoreAliasRepresentation
                                key={keystoreInfo.certificateInfo === undefined ? index : keystoreInfo.certificateInfo.sha256fingerprint}
                                keystoreInfo={keystoreInfo}
                                onDeleteSuccess={this.onDeleteSuccess} />
                        })
                    }
                </CardDeck>
            </React.Fragment>
        );
    }
}

const WrappedKeystoreUploadForm = FormBaseHoc(KeystoreUploadForm);
const WrappedKeystoreAliasForm = FormBaseHoc(KeystoreAliasForm);

export default class KeystoreForm extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            errors: undefined
        };
        this.onKeystoreUploadSuccess = this.onKeystoreUploadSuccess.bind(this);
        this.onAliasSelectionSuccess = this.onAliasSelectionSuccess.bind(this);
        this.onAliasDeleteSuccess = this.onAliasDeleteSuccess.bind(this);
    }

    componentDidMount() {
        fetch("/keystore/aliases")
            .then(response => {
                response.json().then(jsonResponse => {
                    jsonResponse.map(keystoreInfo => {
                        return this.onAliasSelectionSuccess(response.status, keystoreInfo);
                    })
                })
            })
    }

    onKeystoreUploadSuccess(status, json) {
        this.setState({
            uploaded: true,
            stateId: json.stateId,
            aliases: json.aliases,
            errorMessages: {}
        });
    }

    onAliasSelectionSuccess(status, json) {
        let keystoreInfos = [];
        if (this.state.keystoreInfos !== undefined) {
            this.state.keystoreInfos.forEach(keystoreInfo => keystoreInfos.push(keystoreInfo));
        }
        keystoreInfos.push(json);
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
        let uploadFormUrl = "/keystore/upload";
        let aliasFormUrl = "/keystore/select-alias";

        let uploadFormClass = this.state.uploaded === undefined ? "" : "disabled";
        let aliasFormClass = this.state.uploaded !== undefined ? "" : "disabled";

        let keystoreAliasList = null;
        if (this.state.keystoreInfos !== undefined) {
            keystoreAliasList = <KeystoreAliasList keystoreInfos={this.state.keystoreInfos}
                                                   onDeleteSuccess={this.onAliasDeleteSuccess} />;
        }

        return (
            <React.Fragment>
                <WrappedKeystoreUploadForm formId="keystoreUploadForm"
                                           class={uploadFormClass}
                                           header="Keystore Upload"
                                           formUrl={uploadFormUrl}
                                           httpMethod="POST"
                                           successMessage="Keystore was successfully uploaded"
                                           buttonText="Upload"
                                           onSubmitSuccess={this.onKeystoreUploadSuccess} />
                <WrappedKeystoreAliasForm formId="keystoreAliasForm"
                                          class={aliasFormClass}
                                          header="Alias Selection"
                                          formUrl={aliasFormUrl}
                                          httpMethod="POST"
                                          value={this.state.stateId}
                                          aliases={this.state.aliases}
                                          successMessage="Keystore entry was successfully added saved"
                                          buttonText="Save"
                                          onSubmitSuccess={this.onAliasSelectionSuccess} />
                {keystoreAliasList}
            </React.Fragment>
        );
    }
}