import React, {createRef} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import {ErrorMessagesAlert, FormFileField, FormInputField, LoadingSpinner} from "../../base/form-base";
import {Optional} from "../../services/utils";
import ScimClient from "../../scim/scim-client";
import {Alert, Badge, Image} from "react-bootstrap";
import downloadIcon from "../../media/secure-download-icon.png";
import {InfoCircle} from "react-bootstrap-icons";
import {GoFlame, GoThumbsup} from "react-icons/go";
import CertificateList from "../../base/certificate-list";


export default class ApplicationTruststore extends React.Component
{

    constructor(props)
    {
        super(props);
        this.scimResourcePath = "/scim/v2/Truststore";
        this.state = {};
        this.onMergeSuccess = this.onMergeSuccess.bind(this);
        this.onUploadSuccess = this.onUploadSuccess.bind(this);
    }

    onMergeSuccess(mergedAliases)
    {
        this.setState({newAliases: mergedAliases});
    }

    onUploadSuccess(addedCertificateAlias)
    {
        this.setState({newAliases: [addedCertificateAlias]});
    }

    render()
    {
        return (
            <React.Fragment>
                <TruststoreUploadForm scimResourcePath={this.scimResourcePath}
                                      onMergeSuccess={this.onMergeSuccess} />
                <CertificateUploadForm scimResourcePath={this.scimResourcePath}
                                       aliasSelectionResponse={this.state.aliasSelectionResponse}
                                       onUploadSuccess={this.onUploadSuccess} />
                <CertificateEntryList scimResourcePath={this.scimResourcePath}
                                      newAliases={this.state.newAliases} />
            </React.Fragment>
        )
    }
}

class TruststoreUploadForm extends React.Component
{

    constructor(props)
    {
        super(props);
        this.state = {
            success: false,
            showSpinner: false
        }
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient(this.props.scimResourcePath, this.setState);
        this.formReference = createRef();
        this.upload = this.upload.bind(this);
        this.handleCreateResponse = this.handleCreateResponse.bind(this);
    }

    async upload(e)
    {
        e.preventDefault();
        this.setState({
            addedAliases: [],
            duplicateAliases: [],
            duplicateCertificates: []
        });
        let resource = await this.scimClient.getResourceFromFormReference(this.formReference);
        let response = await this.scimClient.createResource(resource);
        this.handleCreateResponse(response);
    }

    handleCreateResponse(response)
    {
        if (response.success)
        {
            response.resource.then(resource =>
            {
                let truststoreUploadResponse = resource.truststoreUploadResponse;

                let addedAliases;
                let duplicateAliases;
                let duplicateCertificates;

                new Optional(truststoreUploadResponse).ifPresent(response =>
                {
                    addedAliases = response.aliases;
                    duplicateAliases = new Optional(response.duplicateAliases).do(val => val.sort())
                                                                              .orElse([]);
                    duplicateCertificates = new Optional(response.duplicateCertificateAliases).do(val => val.sort())
                                                                                              .orElse([])
                })

                this.setState({
                    addedAliases: new Optional(addedAliases).orElse([]),
                    duplicateAliases: new Optional(duplicateAliases).orElse([]),
                    duplicateCertificates: new Optional(duplicateCertificates).orElse([])
                });

                this.props.onMergeSuccess(addedAliases);
            })
            this.setState({success: true});
        }
        else
        {
            this.setState({success: false});
        }
    }

    render()
    {
        let numberOfDuplicateAliases = new Optional(this.state.duplicateAliases).map(val => val.length).orElse(0);
        let numberOfDuplicateCerts = new Optional(this.state.duplicateCertificates).map(val => val.length).orElse(0);

        return (
            <React.Fragment>
                <h2>Truststore Upload</h2>
                <Alert id={"truststoreUploadForm-alert-success"} variant={"success"} show={this.state.success}>
                    <Form.Text><GoThumbsup /> Truststore was successfully merged</Form.Text>
                </Alert>
                <Alert id={"upload-form-alert-duplicate-aliases"} variant={"warning"}
                       show={numberOfDuplicateAliases > 0}>
                    <Form.Text>
                        <GoFlame /> The following aliases could not be added because the alias is
                                    duplicated.<br />
                                    Number of not added aliases: {numberOfDuplicateAliases} <br />
                                    [{this.state.duplicateAliases}]
                    </Form.Text>
                </Alert>
                <Alert id={"upoad-form-alert-duplicate-certificates"} variant={"warning"}
                       show={numberOfDuplicateCerts > 0}>
                    <Form.Text>
                        <GoFlame /> The following aliases could not be added because the certificate is already
                                    present: [{this.state.duplicateCertificates}]
                    </Form.Text>
                </Alert>
                <ErrorMessagesAlert errors={this.state.errors} />
                <Form id={"truststoreUploadForm"} onSubmit={this.upload} ref={this.formReference}>
                    <FormFileField name="truststoreUpload.truststoreFile"
                                   label="Truststore File"
                                   placeholder="Select a truststore file"
                                   onError={fieldName => this.scimClient.getErrors(this.state, fieldName)}
                    />
                    <FormInputField name="truststoreUpload.truststorePassword"
                                    label="Truststore Password"
                                    type="password"
                                    placeholder="Truststore Password"
                                    onError={fieldName => this.scimClient.getErrors(this.state, fieldName)} />

                    <Form.Group as={Row}>
                        <Col sm={{span: 10, offset: 2}}>
                            <Button id={"uploadTruststore"} type="submit">
                                <LoadingSpinner show={this.state.isLoading} /> Upload
                            </Button>
                        </Col>
                    </Form.Group>
                </Form>
            </React.Fragment>
        )
    }
}

class CertificateUploadForm extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {success: false};
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient(props.scimResourcePath, this.setState);
        this.formReference = createRef();
        this.upload = this.upload.bind(this);
        this.handleCreateResponse = this.handleCreateResponse.bind(this);
    }

    async upload(e)
    {
        e.preventDefault();
        this.setState({
            addedAlias: undefined
        });
        let resource = await this.scimClient.getResourceFromFormReference(this.formReference);
        let response = await this.scimClient.createResource(resource);
        this.handleCreateResponse(response);
    }


    handleCreateResponse(response)
    {
        if (response.success)
        {
            response.resource.then(resource =>
            {
                let certificateUploadResponse = resource.certificateUploadResponse;

                let addedAlias;
                new Optional(certificateUploadResponse).ifPresent(response =>
                {
                    addedAlias = response.alias;
                })

                this.setState({
                    addedAlias: addedAlias
                });

                this.props.onUploadSuccess(addedAlias);
            })
            this.setState({success: true});
        }
        else
        {
            this.setState({success: false});
        }
    }

    render()
    {
        return (
            <React.Fragment>
                <h2>Certificate Upload</h2>
                <Alert id={"certificateUploadForm-alert-success"}
                       variant={"success"}
                       show={new Optional(this.state.addedAlias).isPresent()}>
                    <Form.Text><GoThumbsup /> Entry with alias '{this.state.addedAlias}' was successfully
                                              added</Form.Text>
                </Alert>
                <ErrorMessagesAlert errors={this.state.errors} />
                <Form id={"certificateUploadForm"} onSubmit={this.upload} ref={this.formReference}>
                    <FormFileField name="certificateUpload.certificateFile"
                                   label="Certificate File"
                                   placeholder="Select a certificate file"
                                   onError={fieldName => this.scimClient.getErrors(this.state, fieldName)} />
                    <FormInputField name="certificateUpload.alias"
                                    label="Alias"
                                    placeholder="Certificate Alias"
                                    onError={fieldName => this.scimClient.getErrors(this.state, fieldName)} />
                    <Form.Group as={Row}>
                        <Col sm={{span: 10, offset: 2}}>
                            <Button id={"uploadCertificate"} type="Save">
                                <LoadingSpinner show={this.state.isLoading} /> Save
                            </Button>
                        </Col>
                    </Form.Group>
                </Form>
            </React.Fragment>
        )
    }
}

class CertificateEntryList extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {aliases: []};
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient(this.props.scimResourcePath, this.setState);
        this.onDeleteSuccess = this.onDeleteSuccess.bind(this);
    }

    async componentDidMount()
    {
        let response = await this.scimClient.listResources();
        if (response.success)
        {
            response.resource.then(listResponse =>
            {
                let aliases = new Optional(listResponse.Resources[0]).map(val => val.aliases).orElse([]);
                aliases.sort();
                this.setState({
                    aliases: aliases
                })
            })
        }
    }

    componentDidUpdate(prevProps, prevState, snapshot)
    {
        if (new Optional(this.props.newAliases).isPresent() && prevProps.newAliases !== this.props.newAliases)
        {
            let aliases = this.state.aliases.concat(this.props.newAliases);
            aliases.sort();
            this.setState({aliases: aliases, aliasDeleted: undefined});
        }
    }

    onDeleteSuccess(alias)
    {
        let aliases = this.state.aliases;
        const indexOfAlias = aliases.indexOf(alias)
        if (indexOfAlias > -1)
        {
            aliases.splice(indexOfAlias, 1);
        }
        this.setState({
            aliasDeleted: alias,
            aliases: aliases
        })
    }

    render()
    {
        return (
            <React.Fragment>
                <h2 id="application-certificate-info-header">
                    <p>Application Truststore Infos</p>
                    <Badge className="download-keystore-icon">
                        <a id={"truststore-download-link"} href={this.basePath + "/download"}>
                            <Image src={downloadIcon} fluid />
                            <p>Download</p>
                        </a>
                    </Badge>
                </h2>
                <Alert id="card-list-infos-alert"
                       variant={"info"}>
                    <Form.Text>
                        <InfoCircle /> Application Truststore contains
                                       "{new Optional(this.state.aliases).map(val => val.length)
                                                                         .orElse(0)}"
                                       entries
                    </Form.Text>
                </Alert>
                <Alert id="card-list-deletion-success"
                       variant={"success"}
                       show={this.state.aliasDeleted !== undefined}>
                    <Form.Text>
                        <GoThumbsup /> Key entry for alias "{this.state.aliasDeleted}" was successfully deleted
                    </Form.Text>
                </Alert>
                <CertificateList certificateAliases={this.state.aliases}
                                 scimResourcePath={this.props.scimResourcePath}
                                 onDeleteSuccess={this.onDeleteSuccess} />
            </React.Fragment>
        );
    }
}