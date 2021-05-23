import {Tab, Tabs} from "react-bootstrap";
import KeystoreConfigForm from "./keystore-config-form";
import TruststoreConfigForm from "./truststore-config-form";
import ProxyList from "./proxy-config-form";

export default function SystemOverview()
{
    return (
        <Tabs defaultActiveKey="keystore" id="uncontrolled-tab-example">
            <Tab eventKey="keystore" title="Application Keystore">
                <KeystoreConfigForm />
            </Tab>
            <Tab eventKey="truststore" title="Application Truststore">
                <TruststoreConfigForm />
            </Tab>
            <Tab eventKey="proxies" title="Proxies">
                <ProxyList />
            </Tab>
        </Tabs>
    )
}