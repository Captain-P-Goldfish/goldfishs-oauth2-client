import React from "react";
import ConfigPageForm, {FormFileField, FormInputField} from "../base/config-page-form";
import Form from "react-bootstrap/Form";
import {InfoCircle} from "react-bootstrap-icons";
import {Alert, Badge, Image} from "react-bootstrap";
import downloadIcon from "../media/secure-download-icon.png";
import CertificateList from "../base/certificate-list";
import {GoFlame} from "react-icons/go";

export default class TruststoreConfigForm extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            numberOfEntries: null,
            certificateAliases: [],
            duplicateAliases: [],
            duplicateCertificates: []
        };
        this.handleUploadSuccess = this.handleUploadSuccess.bind(this);
        this.onCertificateDelete = this.onCertificateDelete.bind(this);
    }

    async componentDidMount() {
        fetch("/truststore/infos")
            .then(response => response.json())
            .then(response => {
                this.setState({
                    numberOfEntries: response.numberOfEntries,
                    certificateAliases: response.certificateAliases.sort()
                });
            })
    }

    onCertificateDelete(alias) {
        let certificateAliases = this.state.certificateAliases;
        const index = certificateAliases.indexOf(alias);
        if (index > -1) {
            certificateAliases.splice(index, 1);
        }
        this.setState({certificateAliases: certificateAliases.sort()})
        this.componentDidMount();
    }

    handleUploadSuccess(status, response) {

        let duplicateAliases = response.duplicateAliases === undefined ? [] : response.duplicateAliases.sort();
        let duplicateCertificates = response.duplicateCertificates === undefined ? [] : response.duplicateCertificates.sort();

        this.setState({
            duplicateAliases: duplicateAliases,
            duplicateCertificates: duplicateCertificates
        });
        this.componentDidMount();
    }

    render() {
        return (
            <React.Fragment>
                <Alert id={"upload-form-alert-dupliate-aliases"} variant={"warning"}
                       show={this.state.duplicateAliases.length > 0}>
                    <Form.Text>
                        <GoFlame /> The following aliases could not be added because the alias is
                                    duplicated.<br />
                                    Number of not added aliases: {this.state.duplicateAliases.length} <br />
                                    [{this.state.duplicateAliases}]
                    </Form.Text>
                </Alert>
                <Alert id={"upoad-form-alert-dupliate-certificates"} variant={"warning"}
                       show={this.state.duplicateCertificates.length > 0}>
                    <Form.Text>
                        <GoFlame /> The following aliases could not be added because the alias is
                                    duplicated {this.state.duplicateCertificates}
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

                <h2 className="application-certificate-info-header">
                    <p>Application Truststore Infos</p>
                    <Badge className="download-keystore-icon">
                        <a href={"/truststore/download"}>
                            <Image src={downloadIcon} fluid />
                            <p>Download</p>
                        </a>
                    </Badge>
                </h2>
                <Alert id="truststore-infos"
                       variant={"info"}
                       show={this.state.numberOfEntries !== null}>
                    <Form.Text>
                        <InfoCircle /> Application Truststore contains "{this.state.numberOfEntries}" entries
                    </Form.Text>
                </Alert>
                <CertificateList certificateAliases={this.state.certificateAliases}
                                 loadUrl={"/truststore/load-alias"}
                                 deleteUrl={"/truststore/delete-alias"}
                                 onDeleteSuccess={this.onCertificateDelete} />

            </React.Fragment>
        )
    }

}
