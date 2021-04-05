import React from "react";
import ConfigPageForm, {FormFileField, FormInputField, FormSelectField} from "../base/config-page-form";
import {Alert, Badge, Image} from "react-bootstrap";
import {InfoCircle} from "react-bootstrap-icons";
import Form from "react-bootstrap/Form";
import CertificateList from "../base/certificate-list";
import downloadIcon from "../media/secure-download-icon.png";
import {GoThumbsup} from "react-icons/go";
import KeystoreRepresentation from "../base/keystore-representation";

export default class KeystoreConfigForm extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            stateId: "",
            aliasOptions: [],
            numberOfEntries: 0,
            uploadFormDisabled: false,
            selectAliasFormDisabled: true,
            certificateAliases: []
        }
        this.handleUploadSuccess = this.handleUploadSuccess.bind(this);
        this.handleSelectionSuccess = this.handleSelectionSuccess.bind(this);
        this.onAliasDeleteSuccess = this.onAliasDeleteSuccess.bind(this);
    }

    async componentDidMount() {
        fetch("/keystore/infos")
            .then(response => {
                response.json().then(jsonResponse => {
                    let certificateAliases = jsonResponse.certificateAliases === undefined ? [] :
                        jsonResponse.certificateAliases.sort();
                    let numberOfEntries = jsonResponse.numberOfEntries === undefined ? 0 : jsonResponse.numberOfEntries;

                    this.setState({
                        numberOfEntries: numberOfEntries,
                        certificateAliases: certificateAliases
                    });
                })
            })
    }

    handleUploadSuccess(status, response) {
        this.setState({
            stateId: response.stateId,
            aliasOptions: response.aliases,
            uploadFormDisabled: true,
            selectAliasFormDisabled: false,
            aliasDeleted: undefined
        });
    }

    handleSelectionSuccess(status, response) {
        let certificateAliases = [];
        if (this.state.certificateAliases !== undefined) {
            this.state.certificateAliases.forEach(certificateAlias => certificateAliases.push(certificateAlias));
        }
        certificateAliases.push(response.alias);
        this.setState({
            numberOfEntries: this.state.numberOfEntries + 1,
            certificateAliases: certificateAliases.sort(),
            aliasDeleted: undefined
        });
    }

    onAliasDeleteSuccess(alias) {
        let certificateAliases = this.state.certificateAliases.filter(item => item !== alias);
        this.setState({
            numberOfEntries: this.state.numberOfEntries - 1,
            certificateAliases: certificateAliases.sort(),
            aliasDeleted: alias
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

                <KeystoreRepresentation type={"Keystore"}
                                        basePath={"/keystore"}
                                        certificateAliases={this.state.certificateAliases} />

            </React.Fragment>
        );
    }
}
