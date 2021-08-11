import React, {createRef} from "react";
import './jwt-builder.css'
import ScimClient from "../scim/scim-client";
import ScimComponentBasics from "../scim/scim-component-basics";
import Form from "react-bootstrap/Form";
import {FormCheckbox, FormInputField, LoadingSpinner} from "../base/form-base";
import Button from "react-bootstrap/Button";
import {Optional} from "../services/utils";
import {Col, Container, Dropdown, DropdownButton, OverlayTrigger, Row, Tab, Tabs, Tooltip} from "react-bootstrap";
import * as lodash from "lodash";

export default class JwtHandler extends React.Component
{

    constructor(props)
    {
        super(props);
        this.state = {};
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient("/scim/v2/JwtBuilder", this.setState);
    }

    componentDidMount()
    {
        this.scimClient.getResource(null, "/scim/v2/AppInfo").then(response =>
        {
            if (response.success)
            {
                response.resource.then(appInfo =>
                {
                    this.setState({jwtInfo: appInfo.jwtInfo});
                })
            }
        });
        this.scimClient.getResource(null, "/scim/v2/Keystore").then(response =>
        {
            if (response.success)
            {
                response.resource.then(listResponse =>
                {
                    this.setState({keyInfos: listResponse.Resources[0].keyInfos});
                })
            }
        });
    }


    render()
    {
        return (
            <React.Fragment>
                <Tabs defaultActiveKey="jwtbuilder" id="uncontrolled-tab-example">
                    <Tab eventKey="jwtbuilder" title="JWT Builder">
                        <JwtBuilder keyInfos={this.state.keyInfos} jwtInfo={this.state.jwtInfo} />
                    </Tab>
                    <Tab eventKey="jwtparser" title="JWT Parser">
                        <JwtParser keyInfos={this.state.keyInfos} jwtInfo={this.state.jwtInfo} />
                    </Tab>
                </Tabs>
            </React.Fragment>
        )
    }
}

export class JwtBuilder extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {
            jwtBuilder: {},
            header: {},
            body: {},
            addX5Sha256tHeader: false
        }
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient("/scim/v2/JwtBuilder", this.setState);
        this.formReference = createRef();
        this.onCreateSuccess = this.onCreateSuccess.bind(this);
        this.handleKeySelectionSelection = this.handleKeySelectionSelection.bind(this);
        this.handleSignatureAlgorithmSelection = this.handleSignatureAlgorithmSelection.bind(this);
        this.handleEncryptionAlgorithmSelection = this.handleEncryptionAlgorithmSelection.bind(this);
        this.handleContentEncryptionAlgorithmSelection = this.handleContentEncryptionAlgorithmSelection.bind(this);
        this.handleHeaderChange = this.handleHeaderChange.bind(this);
        this.handleBodyChange = this.handleBodyChange.bind(this);
        this.addJwtBody = this.addJwtBody.bind(this);
        this.addDefaultJwtAttributes = this.addDefaultJwtAttributes.bind(this);
        this.addKeyIdToHeader = this.addKeyIdToHeader.bind(this);
        this.handleAddSha256Thumbprint = this.handleAddSha256Thumbprint.bind(this);

        this.scimComponentBasics = new ScimComponentBasics({
            scimClient: this.scimClient,
            formReference: this.formReference,
            getOriginalResource: () => this.props.jwtBuilder,
            getCurrentResource: () => this.state.jwtBuilder,
            setCurrentResource: resource =>
            {
            },
            setState: this.setState,
            onCreateSuccess: this.onCreateSuccess
        });
    }

    componentDidMount()
    {
        this.addHeader();
        this.addDefaultJwtAttributes();
    }


    componentDidUpdate(prevProps, prevState, snapshot)
    {
        if (prevProps.jwtInfo !== this.props.jwtInfo && this.props.jwtInfo !== undefined)
        {
            this.handleSignatureAlgorithmSelection(this.props.jwtInfo.signatureAlgorithms[0])
            this.addHeader();
        }
    }

    onCreateSuccess(resource)
    {
        let jwtArea = document.getElementById("jwt");
        jwtArea.value = resource.jwt;

        this.setState({header: JSON.parse(resource.header)});
        this.addHeader();
    }

    async handleKeySelectionSelection(value)
    {
        await this.setState({selectedKey: value})
        let hiddenKeyIdInputField = document.getElementById("keyId");
        hiddenKeyIdInputField.value = value;
    }

    addKeyIdToHeader()
    {
        let header = this.state.header;
        header["kid"] = this.state.selectedKey
        this.setState({header: header})
        this.addHeader();
    }

    handleSignatureAlgorithmSelection(value)
    {
        let header = this.state.header;
        header["alg"] = value
        header["enc"] = undefined
        this.setState({header: header})
        this.addHeader();
    }

    handleEncryptionAlgorithmSelection(value)
    {
        let header = this.state.header;
        header["alg"] = value
        header["enc"] = new Optional(this.state.header.enc).orElse(new Optional(this.state.jwtInfo)
            .map(val => val.encryptionAlgorithms[0]).orElse(""));
        this.setState({header: header})
        this.addHeader();
    }

    handleContentEncryptionAlgorithmSelection(value)
    {
        let header = this.state.header;
        header["enc"] = value
        this.setState({header: header})
        this.addHeader();
    }

    handleHeaderChange(e)
    {
        try
        {
            let header = JSON.parse(e.target.value);
            this.setState({header: header})
        } catch (Exception)
        {
        }
    }

    handleBodyChange(e)
    {
        try
        {
            let body = JSON.parse(e.target.value);
            this.setState({body: body})
        } catch (Exception)
        {
        }
    }

    addHeader()
    {
        let headerArea = document.getElementById("header");
        headerArea.value = JSON.stringify(this.state.header, undefined, 4);
    }

    async addDefaultJwtAttributes()
    {
        let body = {
            iss: new Optional(this.state.body.iss).orElse(""),
            aud: new Optional(this.state.body.aud).orElse(""),
            sub: new Optional(this.state.body.sub).orElse(""),
            iat: new Date().getTime(),
            exp: new Date().getTime() + (3600 * 24),
            jti: new Optional(this.state.body.jti).orElse("")
        }
        let mergedBody = lodash.merge(this.state.body, body)

        await this.setState({body: mergedBody});
        this.addJwtBody();
    }

    addJwtBody()
    {
        let bodyArea = document.getElementById("body");
        bodyArea.value = JSON.stringify(this.state.body, undefined, 4);
    }

    handleAddSha256Thumbprint(event)
    {
        let isSelected = event.target.checked;
        this.setState({addX5Sha256tHeader: isSelected});
    }

    render()
    {
        let kidMatchesHeader = this.state.selectedKey === this.state.header.kid;

        return (
            <Container>
                <Row>
                    <Col>
                        <Dropdown>
                            <DropdownButton id={"aliases"}
                                            title={"available keys"}
                                            onSelect={this.handleKeySelectionSelection}>
                                {
                                    new Optional(this.props.keyInfos).isPresent() &&
                                    this.props.keyInfos.map((keyInfo) =>
                                    {
                                        return <Dropdown.Item key={keyInfo.alias}
                                                              eventKey={keyInfo.alias}>{keyInfo.alias + " ("
                                                                                        + keyInfo.keyAlgorithm
                                                                                        + "-" + keyInfo.keyLength
                                                                                        + "-bit)"}</Dropdown.Item>
                                    })
                                }
                            </DropdownButton>
                            <p>selected key:

                                <OverlayTrigger
                                    placement={"top"}
                                    overlay={
                                        <Tooltip>
                                            Add as 'kid' to JOSE-header
                                        </Tooltip>
                                    }
                                >
                                <span className={"code"}
                                      style={{
                                          marginLeft: "15px",
                                          color: kidMatchesHeader ? "lightgreen" : "lightcoral",
                                          cursor: "pointer"
                                      }}
                                      onClick={this.addKeyIdToHeader}>
                                   {this.state.selectedKey}
                                </span>
                                </OverlayTrigger>
                            </p>
                        </Dropdown>

                        <Dropdown>
                            <DropdownButton id={"signatureAlgorithms"}
                                            title={"signature algorithms"}
                                            onSelect={this.handleSignatureAlgorithmSelection}>
                                {
                                    new Optional(this.props.jwtInfo).map(val => val["signatureAlgorithms"]).isPresent()
                                    &&
                                    this.props.jwtInfo["signatureAlgorithms"].map((value) =>
                                    {
                                        return <Dropdown.Item key={value}
                                                              eventKey={value}>{value}</Dropdown.Item>
                                    })
                                }
                            </DropdownButton>
                        </Dropdown>

                        <Dropdown>
                            <DropdownButton id={"keyWrapAlgorithms"}
                                            title={"key wrap algorithms"}
                                            onSelect={this.handleEncryptionAlgorithmSelection}>
                                {
                                    new Optional(this.props.jwtInfo).map(val => val["keyWrapAlgorithms"]).isPresent()
                                    &&
                                    this.props.jwtInfo["keyWrapAlgorithms"].map((value) =>
                                    {
                                        return <Dropdown.Item key={value}
                                                              eventKey={value}>{value}</Dropdown.Item>
                                    })
                                }
                            </DropdownButton>
                        </Dropdown>

                        <Dropdown>
                            <DropdownButton id={"contentEncryptionAlgorithms"}
                                            title={"content encryption algorithms"}
                                            onSelect={this.handleContentEncryptionAlgorithmSelection}>
                                {
                                    new Optional(this.props.jwtInfo).isPresent() &&
                                    this.props.jwtInfo["encryptionAlgorithms"].map((value) =>
                                    {
                                        return <Dropdown.Item key={value}
                                                              eventKey={value}>{value}</Dropdown.Item>
                                    })
                                }
                            </DropdownButton>
                        </Dropdown>

                        <Button type={"button"} className={"functional-button"} onClick={this.addDefaultJwtAttributes}>
                            extend by JWT attributes
                        </Button>

                        <Form.Check onChange={this.handleAddSha256Thumbprint}
                                    className={"jwt"}
                                    label={"Add SHA-256 thumbprint to header"} />

                    </Col>
                    <Col sm={"9"}>
                        <Form onSubmit={this.scimComponentBasics.onSubmit} ref={this.formReference}>
                            <FormInputField name="keyId"
                                            type="hidden"
                                            value={this.state.selectedKey}
                                            onError={fieldName => this.scimClient.getErrors(this.state,
                                                fieldName)} />
                            <FormCheckbox name="addX5Sha256tHeader"
                                          type="hidden"
                                          checked={this.state.addX5Sha256tHeader}
                                          onError={fieldName => this.scimClient.getErrors(this.state,
                                              fieldName)} />
                            <FormInputField name="header"
                                            type="text"
                                            as="textarea"
                                            onChange={this.handleHeaderChange}
                                            onError={fieldName => this.scimClient.getErrors(this.state,
                                                fieldName)} />
                            <FormInputField name="body"
                                            type="text"
                                            as="textarea"
                                            onChange={this.handleBodyChange}
                                            onError={fieldName => this.scimClient.getErrors(this.state,
                                                fieldName)} />
                            <Button id={"save"} type="Save">
                                <LoadingSpinner show={this.state.isLoading} /> Create
                            </Button>
                        </Form>
                    </Col>
                </Row>
                <Row>
                    <Col sm={12}>
                        <Form.Control id={"jwt"}
                                      style={{marginTop: "50px"}}
                                      sm={5}
                                      as={"textarea"}
                                      onChange={this.handleBodyChange} />
                    </Col>
                </Row>
            </Container>
        )
    }
}

class JwtParser extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {
            jwtBuilder: {id: "1", header: "", body: ""},
            selectedKey: ""
        };
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient("/scim/v2/JwtBuilder", this.setState);
        this.formReference = createRef();
        this.onUpdateSuccess = this.onUpdateSuccess.bind(this);
        this.handleKeySelectionSelection = this.handleKeySelectionSelection.bind(this);

        this.scimComponentBasics = new ScimComponentBasics({
            scimClient: this.scimClient,
            formReference: this.formReference,
            getOriginalResource: () => this.state.jwtBuilder,
            getCurrentResource: () => this.state.jwtBuilder,
            setCurrentResource: resource =>
            {
            },
            setState: this.setState,
            onUpdateSuccess: this.onUpdateSuccess
        });
    }

    async handleKeySelectionSelection(value)
    {
        await this.setState({selectedKey: value})
        let hiddenKeyIdInputField = document.getElementById("keyId");
        hiddenKeyIdInputField.value = value;
    }

    onUpdateSuccess(resource)
    {
        let jwtBuilder = resource;
        jwtBuilder.header = JSON.stringify(JSON.parse(resource.header), undefined, 4);
        this.setState({jwtBuilder: jwtBuilder});
    }

    render()
    {
        return (
            <Form onSubmit={this.scimComponentBasics.onSubmit} ref={this.formReference}>
                <Container>
                    <Row>
                        <Col sm={3}>
                            <Dropdown>
                                <DropdownButton id={"aliases"}
                                                title={"available keys"}
                                                onSelect={this.handleKeySelectionSelection}>
                                    {
                                        new Optional(this.props.keyInfos).isPresent() &&
                                        this.props.keyInfos.map((keyInfo) =>
                                        {
                                            return <Dropdown.Item key={keyInfo.alias}
                                                                  eventKey={keyInfo.alias}>{keyInfo.alias + " ("
                                                                                            + keyInfo.keyAlgorithm
                                                                                            + "-" + keyInfo.keyLength
                                                                                            + "-bit)"}</Dropdown.Item>
                                        })
                                    }
                                </DropdownButton>
                                <p>
                                    selected key:
                                    <span className={"code"}
                                          style={{marginLeft: "15px", color: "lightgreen"}}>
                                        {this.state.selectedKey}
                                    </span>
                                </p>
                            </Dropdown>
                        </Col>
                    </Row>
                    <Row>
                        <Col sm={"6"} className={"form-group"}>
                            <Form.Control id={"jwt"}
                                          name={"jwt"}
                                          sm={12}
                                          as={"textarea"}
                                          style={{height: "100%"}} />
                        </Col>
                        <Col sm={"6"}>
                            <FormInputField name="keyId"
                                            type="hidden"
                                            value={this.state.selectedKey}
                                            onError={fieldName => this.scimClient.getErrors(this.state,
                                                fieldName)} />
                            <FormInputField name="header"
                                            type="text"
                                            as="textarea"
                                            value={this.state.jwtBuilder.header}
                                            onError={fieldName => this.scimClient.getErrors(this.state,
                                                fieldName)} />
                            <FormInputField name="body"
                                            type="text"
                                            as="textarea"
                                            value={this.state.jwtBuilder.body}
                                            onError={fieldName => this.scimClient.getErrors(this.state,
                                                fieldName)} />
                        </Col>
                    </Row>
                    <Row>
                        <Col>
                            <Button id={"save"} type="Save">
                                <LoadingSpinner show={this.state.isLoading} /> Parse
                            </Button>
                        </Col>
                    </Row>
                </Container>
            </Form>
        )
    }
}
