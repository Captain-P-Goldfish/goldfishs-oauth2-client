import React from "react";
import logo from "./media/logo.svg";
import {Container, Nav, Navbar} from "react-bootstrap";
import {BrowserRouter as Router, Redirect, Route, Switch} from "react-router-dom";
import {LinkContainer} from 'react-router-bootstrap'
import SystemOverview from "./admin/system/system-overview";
import ScimClient from "./scim/scim-client";
import OpenidProvider from "./openid/openid-provider";
import JwtHandler from "./jwt/jwt-handler";
import OpenidClients from "./openid/openid-clients";
import OpenidClientOverview from "./openid/openid-client-overview";
import {AlertListMessages} from "./base/form-base";
import {GoFlame} from "react-icons/go";
import {TokenCategoryList} from "./tokens/token-category";
import {APP_INFO_ENDPOINT, SERVICE_PROVIDER_CONFIG_ENDPOINT} from "./scim/scim-constants";
import {FileParser} from "./file-parser/file-parser";
import {HttpClientRequester} from "./http-requests/http-client-requester";


export const ApplicationInfoContext = React.createContext(null);
export const ScimServiceProviderContext = React.createContext(null);

class Application extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            serviceProviderConfig: {
                bulk: {
                    maxOperations: 5,
                    maxPayloadSize: 2097152
                },
                filter: {
                    maxResults: 5
                }
            }
        }
        this.setState = this.setState.bind(this);
    }

    async componentDidMount() {
        let scimClient = new ScimClient(SERVICE_PROVIDER_CONFIG_ENDPOINT, this.setState);
        scimClient.listResources().then(response => {
            if (response.success) {
                response.resource.then(serviceProviderConfig => {
                    this.setState({serviceProviderConfig: serviceProviderConfig});
                })
            }
        })

        scimClient.getResource(null, APP_INFO_ENDPOINT).then(response => {
            if (response.success) {
                response.resource.then(appInfo => {
                    this.setState({appInfo: appInfo});
                })
            }
        });
    }

    render() {
        return (
            <React.Fragment>
                <Router>
                    <Navbar collapseOnSelect expand="lg" bg="navigation">
                        <Container>
                            <Navbar.Brand href="#home">Captain Goldfish's Rest Client</Navbar.Brand>
                            <Navbar.Toggle aria-controls="responsive-navbar-nav" />
                            <Navbar.Collapse id="responsive-navbar-nav">
                                <Nav className="me-auto" />
                                <Nav>
                                    <Nav.Link href="#">
                                        <img src={logo} className="react-logo" alt="logo" />
                                    </Nav.Link>
                                </Nav>
                            </Navbar.Collapse>
                        </Container>
                    </Navbar>

                    <Navbar bg="navigation-left" className={"navbar-left"} expand="md" variant="dark">
                        <Navbar.Collapse>

                            <Nav className="flex-column">
                                <LinkContainer exact to="/views/openIdProvider">
                                    <Nav.Link>OpenID</Nav.Link>
                                </LinkContainer>
                                <LinkContainer exact to="/views/httpClient">
                                    <Nav.Link>Http Client</Nav.Link>
                                </LinkContainer>
                                <LinkContainer exact to="/views/jwts">
                                    <Nav.Link>JWTs</Nav.Link>
                                </LinkContainer>
                                <LinkContainer exact to="/views/tokenCategories">
                                    <Nav.Link>Storage</Nav.Link>
                                </LinkContainer>
                                <LinkContainer exact to="/views/fileParser">
                                    <Nav.Link>File Parser</Nav.Link>
                                </LinkContainer>
                                <LinkContainer exact to="/views/system">
                                    <Nav.Link>System</Nav.Link>
                                </LinkContainer>
                            </Nav>
                        </Navbar.Collapse>
                    </Navbar>

                    <div className="main">
                        <AlertListMessages variant={"danger"}
                                           icon={<GoFlame />}
                                           messages={(this.state.errors || {}).errorMessages} />

                        <ApplicationInfoContext.Provider value={this.state.appInfo}>
                            <ScimServiceProviderContext.Provider value={this.state.serviceProviderConfig}>
                                {/* A <Switch> looks through its children <Route>s and
                                 renders the first one that matches the current URL. */}
                                <Switch>
                                    <Route path="/views/system">
                                        <SystemOverview />
                                    </Route>
                                    <Route path={"/views/openIdProvider/:providerId/client/:clientId"}
                                           component={OpenidClientOverview} />
                                    <Route path={"/views/openIdProvider/:id/openIdClients"}
                                           render={route => {
                                               return <OpenidClients match={route.match}
                                                                     serviceProviderConfig={this.state.serviceProviderConfig} />
                                           }} />
                                    <Route path="/views/openIdProvider">
                                        <OpenidProvider serviceProviderConfig={this.state.serviceProviderConfig} />
                                    </Route>
                                    <Route path="/views/jwts">
                                        <JwtHandler />
                                    </Route>
                                    <Route path="/views/httpClient">
                                        <HttpClientRequester />
                                    </Route>
                                    <Route path="/views/tokenCategories">
                                        <TokenCategoryList />
                                    </Route>
                                    <Route path="/views/fileParser">
                                        <FileParser />
                                    </Route>
                                    <Route path="/">
                                        <Redirect to="/views/jwts" />
                                    </Route>
                                </Switch>
                            </ScimServiceProviderContext.Provider>
                        </ApplicationInfoContext.Provider>
                    </div>
                </Router>
            </React.Fragment>
        );
    }
}


export default Application;
