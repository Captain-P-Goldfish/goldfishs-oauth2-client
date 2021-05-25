import {Tab, Tabs} from "react-bootstrap";
import ApplicationKeystore from "../admin/system/application-keystore";
import ApplicationTruststore from "../admin/system/application-truststore";

export default function SystemOverview()
{
    return (
        <Tabs defaultActiveKey="keystore" id="uncontrolled-tab-example">
            <Tab eventKey="keystore" title="Application Keystore">
                <ApplicationKeystore />
            </Tab>
            <Tab eventKey="truststore" title="Application Truststore">
                <ApplicationTruststore />
            </Tab>
            {/*<Tab eventKey="proxies" title="Proxies">*/}
            {/*    <ProxyList />*/}
            {/*</Tab>*/}
        </Tabs>
    )
}