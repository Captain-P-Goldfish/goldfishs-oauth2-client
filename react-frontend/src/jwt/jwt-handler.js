import React from "react";
import './jwt-builder.css'
import ScimClient from "../scim/scim-client";
import {Tab, Tabs} from "react-bootstrap";
import JwtParser from "./jwt-parser";
import JwtBuilder from "./jwt-builder";

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
                <Tabs defaultActiveKey="jwtparser" id="uncontrolled-tab-example">
                    <Tab eventKey="jwtparser" title="JWT Parser">
                        <JwtParser keyInfos={this.state.keyInfos} jwtInfo={this.state.jwtInfo} />
                    </Tab>
                    <Tab eventKey="jwtbuilder" title="JWT Builder">
                        <JwtBuilder keyInfos={this.state.keyInfos} jwtInfo={this.state.jwtInfo} />
                    </Tab>
                </Tabs>
            </React.Fragment>
        )
    }
}
