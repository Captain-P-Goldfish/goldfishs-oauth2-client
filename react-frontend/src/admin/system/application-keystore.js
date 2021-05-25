import React, {createRef} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import {ErrorMessagesAlert, FormFileField, FormInputField, FormSelectField, LoadingSpinner} from "../../base/form-base";
import {Optional} from "../../services/utils";
import ScimClient from "../../scim/scim-client";
import {Alert, Badge, Image} from "react-bootstrap";
import downloadIcon from "../../media/secure-download-icon.png";
import {InfoCircle} from "react-bootstrap-icons";
import {GoThumbsup} from "react-icons/go";
import CertificateList from "../../base/certificate-list";
import * as ScimConstants from "../../scim-constants";


export default class ApplicationKeystore extends React.Component
{

    constructor(props)
    {
        super(props);
        this.scimResourcePath = "/scim/v2/Keystore";
        this.state = {};
        this.setAliasSelectionResponse = this.setAliasSelectionResponse.bind(this);
        this.onAliasSelectionSuccess = this.onAliasSelectionSuccess.bind(this);
    }

    setAliasSelectionResponse(resource)
    {
        let copiedResource = JSON.parse(JSON.stringify(resource));
        this.setState({aliasSelectionResponse: copiedResource})
    }

    onAliasSelectionSuccess(resource)
    {
        this.setState({newAlias: {value: resource[ScimConstants.CERT_URI].alias}})
    }

    render()
    {
        return (
            <React.Fragment>
                <KeystoreUpload scimResourcePath={this.scimResourcePath}
                                setAliasSelectionResponse={this.setAliasSelectionResponse} />
                <AliasSelection scimResourcePath={this.scimResourcePath}
                                aliasSelectionResponse={this.state.aliasSelectionResponse}
                                onCreateSuccess={this.onAliasSelectionSuccess} />
                <KeystoreEntryList scimResourcePath={this.scimResourcePath}
                                   newAlias={this.state.newAlias} />
            </React.Fragment>
        )
    }
}

class KeystoreUpload extends React.Component
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
                this.props.setAliasSelectionResponse(resource);
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
                <h2>Keystore Upload</h2>
                <Alert id={"uploadForm-alert-success"} variant={"success"} show={this.state.success}>
                    <Form.Text><GoThumbsup /> Keystore was successfully uploaded</Form.Text>
                </Alert>
                <ErrorMessagesAlert errors={this.state.errors} />
                <Form id={"uploadForm"} onSubmit={this.upload} ref={this.formReference}>
                    <FormFileField name="fileUpload.keystoreFile"
                                   label="Keystore File"
                                   placeholder="Select a keystore file"
                                   onError={fieldName => this.scimClient.getErrors(this.state, fieldName)} />
                    <FormInputField name="fileUpload.keystorePassword"
                                    label="Keystore Password"
                                    type="password"
                                    placeholder="Keystore Password"
                                    onError={fieldName => this.scimClient.getErrors(this.state, fieldName)} />
                    <Form.Group as={Row}>
                        <Col sm={{span: 10, offset: 2}}>
                            <Button id={"upload"} type="submit">
                                <LoadingSpinner show={this.state.isLoading} /> Upload
                            </Button>
                        </Col>
                    </Form.Group>
                </Form>
            </React.Fragment>
        )
    }
}

class AliasSelection extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {success: false};
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient(props.scimResourcePath, this.setState);
        this.formReference = createRef();
        this.save = this.save.bind(this);
    }

    async save(e)
    {
        e.preventDefault();
        let resource = await this.scimClient.getResourceFromFormReference(this.formReference);
        let response = await this.scimClient.createResource(resource);
        if (response.success)
        {
            response.resource.then(resource =>
            {
                this.props.onCreateSuccess(resource);
                this.setState({newAlias: resource[ScimConstants.CERT_URI].alias});
            })
        }
        else
        {
            this.setState({newAlias: undefined});
        }
    }

    render()
    {
        let stateId = new Optional(this.props.aliasSelectionResponse).map(val => val.aliasSelection)
                                                                     .map(val => val.stateId)
                                                                     .orElse(0);
        let aliases = new Optional(this.props.aliasSelectionResponse).map(val => val.aliasSelection)
                                                                     .map(val => val.aliases)
                                                                     .orElse([]);
        let className = new Optional(this.props.aliasSelectionResponse).map(val => "").orElse("disabled")
        return (
            <React.Fragment>
                <h2>Alias Selection</h2>
                <Alert id={"aliasSelectionForm-alert-success"}
                       variant={"success"}
                       show={new Optional(this.state.newAlias).isPresent()}>
                    <Form.Text><GoThumbsup /> Entry with alias '{this.state.newAlias}' was successfully
                                              added</Form.Text>
                </Alert>
                <ErrorMessagesAlert errors={this.state.errors} />
                <Form id={"aliasSelectionForm"} onSubmit={this.save} ref={this.formReference} className={className}>
                    <FormInputField name="aliasSelection.stateId"
                                    type="hidden"
                                    value={stateId}
                                    onError={fieldName => this.scimClient.getErrors(this.state, fieldName)} />
                    <FormSelectField name="aliasSelection.aliases"
                                     label="Alias"
                                     options={aliases}
                                     onError={fieldName => this.scimClient.getErrors(this.state, fieldName)} />
                    <FormInputField name="aliasSelection.aliasOverride"
                                    label="Alias Override"
                                    type="text"
                                    placeholder="Store under another alias"
                                    onError={fieldName => this.scimClient.getErrors(this.state, fieldName)} />
                    <FormInputField name="aliasSelection.privateKeyPassword"
                                    label="Private Key Password"
                                    type="password"
                                    placeholder="Optional if password is identical to keystore password"
                                    onError={fieldName => this.scimClient.getErrors(this.state, fieldName)} />
                    <Form.Group as={Row}>
                        <Col sm={{span: 10, offset: 2}}>
                            <Button id={"save"} type="Save">
                                <LoadingSpinner show={this.state.isLoading} /> Save
                            </Button>
                        </Col>
                    </Form.Group>
                </Form>
            </React.Fragment>
        )
    }
}

class KeystoreEntryList extends React.Component
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
                this.setState({
                    aliases: new Optional(listResponse.Resources[0]).map(val => val.aliases)
                                                                    .orElse([])
                })
            })
        }
    }

    componentDidUpdate(prevProps, prevState, snapshot)
    {
        if (prevProps.newAlias !== this.props.newAlias && new Optional(this.props.newAlias).map(val => val.value)
                                                                                           .isPresent())
        {
            let aliases = [...this.state.aliases, this.props.newAlias.value];
            aliases.sort();
            this.setState({aliases: [...this.state.aliases, this.props.newAlias.value], aliasDeleted: undefined});

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
                    <p>Application Keystore Infos</p>
                    <Badge className="download-keystore-icon">
                        <a id={"keystore-download-link"} href={this.basePath + "/download"}>
                            <Image src={downloadIcon} fluid />
                            <p>Download</p>
                        </a>
                    </Badge>
                </h2>
                <Alert id="card-list-infos-alert"
                       variant={"info"}>
                    <Form.Text>
                        <InfoCircle /> Application Keystore contains
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