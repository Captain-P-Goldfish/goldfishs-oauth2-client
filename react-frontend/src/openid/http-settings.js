import React, {createRef} from "react";
import ScimClient from "../scim/scim-client";
import {Alert} from "react-bootstrap";
import Form from "react-bootstrap/Form";
import {GoThumbsup} from "react-icons/go";
import {ErrorMessagesAlert, FormCheckbox, FormInputField, FormObjectList, LoadingSpinner} from "../base/form-base";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import ScimComponentBasics from "../scim/scim-component-basics";
import {Optional} from "../services/utils";
import {HTTP_CLIENT_SETTINGS_ENDPOINT, KEYSTORE_ENDPOINT, PROXY_ENDPOINT} from "../scim/scim-constants";

export default class HttpSettings extends React.Component
{

  constructor(props)
  {
    super(props);

    let httpSettings = this.props.client['urn:ietf:params:scim:schemas:captaingoldfish:2.0:HttpClientSettings'];
    this.state = {
      success: false,
      showSpinner: false,
      settings: {},
      proxies: [],
      keyInfos: [],
      originalSetting: JSON.parse(JSON.stringify(httpSettings)),
      httpSettings: httpSettings,
      enableHostnameVerifier: httpSettings.useHostnameVerifier
    };
    this.setState = this.setState.bind(this);
    let resourcePath = HTTP_CLIENT_SETTINGS_ENDPOINT;
    this.scimClient = new ScimClient(resourcePath, this.setState);
    this.formReference = createRef();

    this.scimComponentBasics = new ScimComponentBasics({
      scimClient: this.scimClient,
      formReference: this.formReference,
      getOriginalResource: () => new Optional(this.state.originalSetting).orElse({}),
      getCurrentResource: () => this.state.httpSettings,
      setCurrentResource: resource => this.setState({client: resource}),
      setState: this.setState,
      onCreateSuccess: (resource) => console.error("create must not be called!"),
      onUpdateSuccess: this.onUpdateSuccess
    });

    this.onUpdateSuccess = this.onUpdateSuccess.bind(this);
  }

  async componentDidMount()
  {
    await this.loadProxies();
    await this.loadKeystoreInfos();
  }

  async loadProxies()
  {
    let proxyResourcePath = PROXY_ENDPOINT;
    await new ScimClient(proxyResourcePath, this.setState).listResources().then(response =>
    {
      if (response.success)
      {
        response.resource.then(listResponse =>
        {
          this.setState({proxies: listResponse.Resources || []});
        });
      } else
      {
        response.resource.then(errorResponse =>
        {
          this.setState({
            errors: {
              errorMessages: [errorResponse.detail]
            }
          });
        });
      }
    });
  }

  async loadKeystoreInfos()
  {
    let keystoreResourcePath = KEYSTORE_ENDPOINT;
    this.scimClient.getResource(undefined, keystoreResourcePath).then(response =>
    {
      if (response.success)
      {
        response.resource.then(listResponse =>
        {
          this.setState({
            keyInfos: new Optional(listResponse).map(lr => lr.Resources).map(r => r[0]).map(
              keystore => keystore.keyInfos).orElse([])
          });
        });
      } else
      {
        response.resource.then(errorResponse =>
        {
          this.setState({
            errors: {
              errorMessages: [errorResponse.detail]
            }
          });
        });
      }
    });
  }

  onUpdateSuccess()
  {

  }

  render()
  {
    let proxies = [];
    this.state.proxies.forEach(proxy =>
    {
      proxies.push({
        id: proxy.id,
        value: proxy.hostname + ":" + proxy.port
      });
    });

    let aliases = [];
    this.state.keyInfos.forEach(keyInfo =>
    {
      if (keyInfo.hasPrivateKey === true)
      {
        aliases.push({
          id: keyInfo.alias,
          value: keyInfo.alias + " (" + keyInfo.keyAlgorithm + "-" + keyInfo.keyLength + ")"
        });
      }
    });

    return (
      <React.Fragment>
        <h2>Http Client Settings for {this.props.client.clientId}</h2>
        <Alert id={"uploadForm-alert-success"} variant={"success"} show={this.state.success}>
          <Form.Text><GoThumbsup/> Http Client Settings were successfully saved</Form.Text>
        </Alert>
        <ErrorMessagesAlert errors={this.state.errors}/>
        <Form onSubmit={this.scimComponentBasics.onSubmit} ref={this.formReference}>
          <FormInputField name="id"
                          isHidden={true}
                          type="string"
                          readOnly={true}
                          value={this.state.httpSettings.id}
                          onError={fieldName => this.scimClient.getErrors(this.state, fieldName)}/>
          <FormInputField name="openIdClientReference"
                          isHidden={true}
                          type="number"
                          readOnly={true}
                          value={new Optional(this.props.client.id).orElse("")}
                          onError={fieldName => this.scimClient.getErrors(this.state, fieldName)}/>
          <FormInputField name="requestTimeout"
                          type="number"
                          label="Request Timeout"
                          value={this.state.httpSettings.requestTimeout}
                          onChange={e => this.scimComponentBasics.updateInput(e.target.name, e.target.value)}
                          onError={fieldName => this.scimClient.getErrors(this.state, fieldName)}/>
          <FormInputField name="connectionTimeout"
                          label="Connection Timeout"
                          type="number"
                          value={this.state.httpSettings.connectionTimeout}
                          onChange={e => this.scimComponentBasics.updateInput(e.target.name, e.target.value)}
                          onError={fieldName => this.scimClient.getErrors(this.state, fieldName)}/>
          <FormInputField name="socketTimeout"
                          label="Socket Timeout"
                          type="number"
                          value={this.state.httpSettings.socketTimeout}
                          onChange={e => this.scimComponentBasics.updateInput(e.target.name, e.target.value)}
                          onError={fieldName => this.scimClient.getErrors(this.state, fieldName)}/>
          <FormCheckbox id={"useHostnameVerifier"}
                        name="useHostnameVerifier"
                        label="Enable Hostname Verifier"
                        checked={this.state.enableHostnameVerifier}
                        onChange={(e) => this.setState({enableHostnameVerifier: e.target.checked})}
                        onError={fieldName => this.scimClient.getErrors(this.state, fieldName)}/>
          <FormObjectList name={"proxyReference"}
                          label={"Proxy"}
                          selections={["", ...proxies]}
                          selected={this.state.httpSettings.proxyReference}
                          onChange={e => this.scimComponentBasics.updateInput(e.target.name, e.target.value)}
                          onError={fieldName => this.scimClient.getErrors(
                            this.state, fieldName)}/>
          <FormObjectList name={"tlsClientAuthAliasReference"}
                          label={"TLS Client Auth Key Reference"}
                          selections={["", ...aliases]}
                          selected={this.state.httpSettings.tlsClientAuthAliasReference}
                          onChange={e => this.scimComponentBasics.updateInput(e.target.name, e.target.value)}
                          onError={fieldName => this.scimClient.getErrors(
                            this.state, fieldName)}/>

          <Form.Group as={Row}>
            <Col sm={{
              span: 10,
              offset: 2
            }}>
              <Button id={"upload"} type="submit">
                <LoadingSpinner show={this.state.isLoading}/> Save
              </Button>
            </Col>
          </Form.Group>
        </Form>
      </React.Fragment>
    );
  }
}
