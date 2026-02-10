import React from "react";
import {Tab, Tabs} from "react-bootstrap";
import ScimClient from "../scim/scim-client";
import {ArrowLeftCircle} from "react-bootstrap-icons";
import {LinkContainer} from "react-router-bootstrap";
import HttpSettings from "./http-settings";
import {Optional} from "../services/utils";
import OpenidClientWorkflow from "./openid-client-workflow";
import {ApplicationInfoContext} from "../app";
import {useParams} from "react-router-dom";

export default function OpenidClientOverviewRoute(props) {
  const params = useParams();
  return <OpenidClientOverview {...props} params={params} />;
}

export class OpenidClientOverview extends React.Component
{
  constructor(props)
  {
    super(props);
    this.state = {}
    this.setState = this.setState.bind(this);
  }

  async componentDidMount()
  {
    let openIdProviderId = this.props.params.providerId;
    let clientId = this.props.params.clientId;

    let openIdProviderResourcePath = "/scim/v2/OpenIdProvider";
    new ScimClient(openIdProviderResourcePath, this.setState).getResource(openIdProviderId).then(response =>
    {
      if (response.success)
      {
        response.resource.then(openIdProvider =>
        {
          this.setState({provider: openIdProvider});
        })
      } else
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
    new ScimClient(clientResourcePath, this.setState).getResource(clientId).then(response =>
    {
      if (response.success)
      {
        response.resource.then(client =>
        {
          this.setState({client: client});
        })
      } else
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
    let provider = new Optional(this.state.provider);
    let client = new Optional(this.state.client);

    return (
      <React.Fragment>
        <LinkContainer to={`/views/openIdProvider/${this.props.params.providerId}/openIdClients`}>
          <a href="/#" className="action-link mobile-aware">
            <h5 className="mobile-aware-title">
              <ArrowLeftCircle className="mobile-aware-icon" />
              <span className="mobile-aware-text">
                Back to
                <span className="mobile-aware-provider">
                  {" \""}{provider.map(val => val.name).orElse("")}{"\" "}
                </span>
                Overview
              </span>
            </h5>
          </a>
        </LinkContainer>

        <h5 className="mobile-aware mb-3 mt-2">
          Client:{" "}
          <span className="mobile-aware-muted" style={{color: "lightgray"}}>
            {client.map(c => c.clientId).orElse("")}
          </span>
        </h5>

        <Tabs defaultActiveKey="workflow" id="uncontrolled-tab-example">
          <Tab eventKey="workflow" title="OpenID Workflow">
            {
              new Optional(this.state.client).isPresent() &&
              <ApplicationInfoContext.Consumer>
                {appInfo =>
                  <OpenidClientWorkflow client={this.state.client}
                                        originalRedirectUri={appInfo.authCodeRedirectUri}/>
                }
              </ApplicationInfoContext.Consumer>
            }
          </Tab>
          <Tab eventKey="clients" title="HTTP Settings">
            {
              new Optional(this.state.client).isPresent() &&
              <HttpSettings client={this.state.client}/>
            }
          </Tab>
        </Tabs>
      </React.Fragment>
    )
  }
}
