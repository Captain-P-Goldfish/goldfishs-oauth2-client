import React, {createRef} from "react";
import './jwt-builder.css'
import ScimClient from "../scim/scim-client";
import ScimComponentBasics from "../scim/scim-component-basics";
import Form from "react-bootstrap/Form";
import {FormInputField, LoadingSpinner} from "../base/form-base";
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
            body: {}
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
        let headerArea = document.getElementById("jwt");
        headerArea.value = resource.body;
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

                    </Col>
                    <Col sm={"9"}>
                        <Form onSubmit={this.scimComponentBasics.onSubmit} ref={this.formReference}>
                            <FormInputField name="keyId"
                                            type="hidden"
                                            value={this.state.selectedKey}
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
                                      sm={5}
                                      as={"textarea"}
                                      onChange={this.handleBodyChange} />
                    </Col>
                </Row>
            </Container>
        )
    }
}
