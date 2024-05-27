import React, {useEffect, useState} from "react";
import {loadKeystoreInfos, loadProxies} from "../scim/scim-constants";
import Form from "react-bootstrap/Form";
import {FormCheckbox, FormInputField, FormObjectList} from "../base/form-base";
import * as lodash from "lodash";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import {Optional} from "../services/utils";
import {Alert} from "react-bootstrap";

export function HttpClientSettings(props)
{

    const [proxies, setProxies] = useState([]);
    const [keys, setKeys] = useState([]);

    const id = props.clientSettings.id || -1;
    const [requestTimeout, setRequestTimeout] = useState(props.clientSettings.requestTimeout || 5);
    const [connectTimeout, setConnectTimeout] = useState(props.clientSettings.connectionTimeout || 5);
    const [socketTimeout, setSocketTimeout] = useState(props.clientSettings.socketTimeout || 5);
    const [useHostnameVerifier, setUseHostnameVerifier] = useState(
        new Optional(props.clientSettings.useHostnameVerifier).orElse(
            true));
    const [selectedProxy, setSelectedProxy] = useState(props.clientSettings.proxyReference);
    const [selectedKey, setSelectedKey] = useState(props.clientSettings.tlsClientAuthAliasReference);

    const [updated, setUpdated] = useState(false);
    const [error, setError] = useState();

    useEffect(() =>
              {
                  loadProxies(setProxies);
                  loadKeystoreInfos(setKeys);
              }, []);

    return <React.Fragment>
        <h2>Http Client Settings</h2>
        <Alert variant={"success"} show={updated} dismissible onClick={() => setUpdated(false)}>
            Client Settings were successfully updated
        </Alert>
        <Alert variant={"danger"} show={new Optional(error).isPresent()} dismissible onClick={() => setError(null)}>
            {error}
        </Alert>
        <Form>
            <FormInputField name="id"
                            isHidden={true}
                            type="string"
                            readOnly={true}
                            value={id}
                            onError={fieldName =>
                            {
                            }} />
            <FormInputField name="requestTimeout"
                            type="number"
                            label="Request Timeout"
                            value={requestTimeout}
                            onChange={e => setRequestTimeout(e.target.value)}
                            onError={fieldName =>
                            {
                            }} />
            <FormInputField name="connectionTimeout"
                            label="Connection Timeout"
                            type="number"
                            value={connectTimeout}
                            onChange={e => setConnectTimeout(e.target.value)}
                            onError={fieldName =>
                            {
                            }} />
            <FormInputField name="socketTimeout"
                            label="Socket Timeout"
                            type="number"
                            value={socketTimeout}
                            onChange={e => setSocketTimeout(e.target.value)}
                            onError={fieldName =>
                            {
                            }} />
            <FormCheckbox id={"useHostnameVerifier"}
                          name="useHostnameVerifier"
                          label="Enable Hostname Verifier"
                          checked={useHostnameVerifier}
                          onChange={(e) => setUseHostnameVerifier(e.target.checked)}
                          onError={fieldName =>
                          {
                          }} />
            <FormObjectList name={"proxyReference"}
                            label={"Proxy"}
                            selections={[{}, ...lodash.map(proxies, val =>
                            {
                                return {
                                    id: val.id,
                                    value: val.hostname + ":" + val.port
                                };
                            })]}
                            selected={selectedProxy}
                            onChange={e => setSelectedProxy(e.target.value)}
                            onError={fieldName =>
                            {
                            }} />
            <FormObjectList name={"tlsClientAuthAliasReference"}
                            label={"TLS Client Auth Key Reference"}
                            selections={[{}, ...lodash.map(keys, val =>
                            {
                                return {
                                    id: val.alias,
                                    value: val.alias + " (" + val.keyAlgorithm + " " + val.keyLength + " bit)"
                                };
                            })]}
                            selected={selectedKey}
                            onChange={e => setSelectedKey(e.target.value)}
                            onError={fieldName =>
                            {
                            }} />
            <Form.Group as={Row}>
                <Col sm={{
                    span: 10,
                    offset: 2
                }}>
                    <Button id={"upload"} onClick={() =>
                    {
                        setUpdated(false);
                        setError(null);

                        let httpClientSettings = {
                            id: id,
                            requestTimeout: requestTimeout,
                            connectionTimeout: connectTimeout,
                            socketTimeout: socketTimeout,
                            useHostnameVerifier: useHostnameVerifier,
                            proxyReference: selectedProxy,
                            tlsClientAuthAliasReference: selectedKey
                        };
                        try
                        {
                            props.save(httpClientSettings);
                            setUpdated(true);
                        }
                        catch (e)
                        {
                            setError(e.message);
                        }
                    }}>
                        Update
                    </Button>
                </Col>
            </Form.Group>
        </Form>
    </React.Fragment>;
}

