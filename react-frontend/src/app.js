import React from "react";
import logo from "./logo.svg";
import {Container, Nav, Navbar} from "react-bootstrap";
import {BrowserRouter as Router, Route, Switch} from "react-router-dom";
import {LinkContainer} from 'react-router-bootstrap'
import SystemOverview from "./admin/system/system-overview";
import ScimClient from "./scim/scim-client";
import OpenidProvider from "./openid/openid-provider";
import JwtHandler from "./jwt/jwt-builder";
import OpenidClients from "./openid/openid-clients";
import OpenidClientOverview from "./openid/openid-client-overview";


class Application extends React.Component
{

    constructor(props)
    {
        super(props);
        this.state = {configLoaded: false}
        this.helloWorld = this.helloWorld.bind(this);
    }

    helloWorld(object)
    {
        this.setState(object);
    }

    async componentDidMount()
    {
        if (this.state.configLoaded)
        {
            return;
        }
        let scimClient = new ScimClient("/scim/v2/ServiceProviderConfig", this.helloWorld);
        let serviceProvider = await scimClient.listResources();
        serviceProvider.resource.then(resource =>
        {
            window.MAX_RESULTS = resource.filter.maxResults;
            window.MAX_OPERATIONS = resource.bulk.maxOperations;
        })
        this.setState({configLoaded: true})
    }

    render()
    {
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

                                <LinkContainer exact to="/">
                                    <Nav.Link>Home</Nav.Link>
                                </LinkContainer>
                                <LinkContainer exact to="/openIdProvider">
                                    <Nav.Link>OpenID</Nav.Link>
                                </LinkContainer>
                                <LinkContainer exact to="/jwts">
                                    <Nav.Link>JWTs</Nav.Link>
                                </LinkContainer>
                                <LinkContainer exact to="/system">
                                    <Nav.Link>System</Nav.Link>
                                </LinkContainer>
                            </Nav>
                        </Navbar.Collapse>
                    </Navbar>

                    <div className="main">
                        {/* A <Switch> looks through its children <Route>s and
                         renders the first one that matches the current URL. */}
                        <Switch>
                            <Route path="/system">
                                <SystemOverview />
                            </Route>
                            <Route path={"/openIdProvider/:providerId/client/:clientId"}
                                   component={OpenidClientOverview} />
                            <Route path={"/openIdProvider/:id/openIdClients"}
                                   component={OpenidClients} />
                            <Route path="/openIdProvider">
                                <OpenidProvider />
                            </Route>
                            <Route path="/jwts">
                                <JwtHandler />
                            </Route>
                            <Route path="/">
                                <h2>Welcome</h2>
                            </Route>
                        </Switch>
                    </div>
                </Router>
            </React.Fragment>
        );
    }
}


export default Application;
