import React from "react";
import ConfigPageForm, {FormFileField, FormInputField} from "../base/config-page-form";
import Form from "react-bootstrap/Form";
import {Alert} from "react-bootstrap";
import {GoFlame} from "react-icons/go";
import KeystoreRepresentation from "../base/keystore-representation";

export default class TruststoreConfigForm extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            numberOfEntries: null,
            certificateAliases: [],
            duplicateAliases: [],
            duplicateCertificates: []
        };
        this.componentDidMount = this.componentDidMount.bind(this);
        this.handleUploadSuccess = this.handleUploadSuccess.bind(this);
    }

    componentDidMount() {
        fetch("/truststore/infos")
            .then(response => response.json())
            .then(response => {
                let certificateAliases = this.state.certificateAliases;
                if (response.certificateAliases !== undefined) {
                    certificateAliases = certificateAliases.concat(response.certificateAliases).sort();
                }
                this.setState({
                    numberOfEntries: response.numberOfEntries,
                    certificateAliases: certificateAliases
                });
            })
    }

    handleUploadSuccess(status, response) {
        let duplicateAliases = response.duplicateAliases === undefined ? [] : response.duplicateAliases.sort();
        let duplicateCertificates = response.duplicateCertificates === undefined ? [] : response.duplicateCertificates.sort();
        let certificateAliases = this.state.certificateAliases;
        if (response.aliases !== undefined) {
            certificateAliases = certificateAliases.concat(response.aliases).sort();
        }

        this.setState({
            duplicateAliases: duplicateAliases,
            duplicateCertificates: duplicateCertificates,
            certificateAliases: certificateAliases
        });
    }

    render() {
        return (
            <React.Fragment>
                <Alert id={"upload-form-alert-duplicate-aliases"} variant={"warning"}
                       show={this.state.duplicateAliases.length > 0}>
                    <Form.Text>
                        <GoFlame /> The following aliases could not be added because the alias is
                                    duplicated.<br />
                                    Number of not added aliases: {this.state.duplicateAliases.length} <br />
                                    [{this.state.duplicateAliases}]
                    </Form.Text>
                </Alert>
                <Alert id={"upoad-form-alert-duplicate-certificates"} variant={"warning"}
                       show={this.state.duplicateCertificates.length > 0}>
                    <Form.Text>
                        <GoFlame /> The following aliases could not be added because the certificate is already
                                    present: [{this.state.duplicateCertificates}]
                    </Form.Text>
                </Alert>

                <ConfigPageForm formId="truststoreUploadForm"
                                header="Truststore Upload"
                                httpMethod="POST"
                                submitUrl="/truststore/add"
                                onSubmitSuccess={this.handleUploadSuccess}
                                buttonId="truststoreUploadButton"
                                buttonText="Upload"
                                successMessage="Truststore was successfully merged into application keystore">
                    {({onChange, onError}) => (
                        <React.Fragment>
                            <FormFileField name="truststoreFile"
                                           label="Truststore File"
                                           placeholder="Select a truststore file"
                                           onChange={onChange}
                                           onError={onError}
                            />
                            <FormInputField name="truststorePassword"
                                            label="Truststore Password"
                                            type="password"
                                            placeholder="Truststore Password"
                                            onChange={onChange}
                                            onError={onError} />
                        </React.Fragment>
                    )}
                </ConfigPageForm>
                <ConfigPageForm formId="certUploadForm"
                                header="Certificate File Upload"
                                httpMethod="POST"
                                submitUrl="/truststore/add"
                                onSubmitSuccess={this.handleUploadSuccess}
                                buttonId="certFileUploadButton"
                                buttonText="Upload"
                                successMessage="Certificate was successfully added to application keystore">
                    {({onChange, onError}) => (
                        <React.Fragment>
                            <FormFileField name="certificateFile"
                                           label="Certificate File"
                                           placeholder="Select a certificate file"
                                           onChange={onChange}
                                           onError={onError} />
                            <FormInputField name="alias"
                                            label="Alias"
                                            placeholder="Certificate Alias"
                                            onChange={onChange}
                                            onError={onError} />
                        </React.Fragment>
                    )}
                </ConfigPageForm>

                <KeystoreRepresentation type={"Truststore"}
                                        basePath={"/truststore"}
                                        certificateAliases={this.state.certificateAliases} />

            </React.Fragment>
        )
    }

}
