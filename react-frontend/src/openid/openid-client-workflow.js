import React from "react";
import {Tab, Tabs} from "react-bootstrap";
import ScimClient from "../scim/scim-client";
import {ArrowLeftCircle} from "react-bootstrap-icons";
import {LinkContainer} from "react-router-bootstrap";

export default class OpenidClientWorkflow extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {
            provider: {},
            client: {}
        }
        this.setState = this.setState.bind(this);
    }

    async componentDidMount()
    {
        let openIdProviderId = this.props.match.params.providerId;
        let clientId = this.props.match.params.clientId;

        let openIdProviderResourcePath = "/scim/v2/OpenIdProvider";
        await new ScimClient(openIdProviderResourcePath, this.setState).getResource(openIdProviderId).then(response =>
        {
            if (response.success)
            {
                response.resource.then(openIdProvider =>
                {
                    this.setState({provider: openIdProvider});
                })
            }
            else
            {
                response.resource.then(errorResponse =>
                {
                    this.setState({
                        errors: {
                            errorMessages: [errorResponse.detail]
                        }
                    })
                })
            }
        });

        let clientResourcePath = "/scim/v2/OpenIdClient";
        await new ScimClient(clientResourcePath, this.setState).getResource(clientId).then(response =>
        {
            if (response.success)
            {
                response.resource.then(openIdProvider =>
                {
                    this.setState({client: openIdProvider});
                })
            }
            else
            {
                response.resource.then(errorResponse =>
                {
                    this.setState({
                        errors: {
                            errorMessages: [errorResponse.detail]
                        }
                    })
                })
            }
        });
    }


    render()
    {
        return (
            <React.Fragment>
                <LinkContainer exact
                               to={"/openIdProvider/" + this.state.provider.id + "/openIdClients"}>
                    <a>
                        <h5 style={{height: "35px", padding: "0", paddingLeft: "10px"}}>
                            <ArrowLeftCircle style={{color: "bisque"}} height={"35px"} size={"25px"} />
                            <span style={{marginLeft: "15px"}}>Back to "{this.state.provider.name}" Overview</span>
                        </h5>
                    </a>
                </LinkContainer>

                <h5>Client: {this.state.client.clientId}</h5>

                <Tabs defaultActiveKey="workflow" id="uncontrolled-tab-example">
                    <Tab eventKey="workflow" title="OpenID Workflow">
                    </Tab>
                    <Tab eventKey="clients" title="HTTP Settings">
                    </Tab>
                </Tabs>
            </React.Fragment>
        )
    }
}