import React from "react";
import {Tab, Tabs} from "react-bootstrap";
import OpenidClients from "./openid-clients";
import ScimClient from "../scim/scim-client";
import {ArrowLeftCircle} from "react-bootstrap-icons";
import {LinkContainer} from "react-router-bootstrap";

export default class OpenidProviderOverview extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {}
        this.setState = this.setState.bind(this);
        let openIdProviderResourcePath = "/scim/v2/OpenIdProvider";
        this.scimClient = new ScimClient(openIdProviderResourcePath, this.setState);
    }

    async componentDidMount()
    {
        let openIdProviderId = this.props.match.params.id;
        await this.scimClient.getResource(openIdProviderId).then(response =>
        {
            if (response.success)
            {
                response.resource.then(openIdProvider =>
                {
                    this.setState({openIdProvider: openIdProvider});
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
        })
    }


    render()
    {
        return (
            <React.Fragment>
                <LinkContainer exact
                               to={"/openIdProvider/"}>
                    <a>
                        <h5 style={{height: "35px", padding: "0", paddingLeft: "10px"}}>
                            <ArrowLeftCircle style={{color: "bisque"}} height={"35px"} size={"25px"} />
                            <span style={{marginLeft: "15px"}}>Back to Provider Overview</span>
                        </h5>
                    </a>
                </LinkContainer>

                <Tabs defaultActiveKey="workflow" id="uncontrolled-tab-example">
                    <Tab eventKey="workflow" title="OpenID Workflow">

                    </Tab>
                    <Tab eventKey="clients" title="OpenID Clients">
                        <OpenidClients provider={this.state.openIdProvider} />
                    </Tab>
                </Tabs>
            </React.Fragment>
        )
    }
}