import React, {useState} from "react";
import {Alert, Card, Collapse} from "react-bootstrap";
import {CaretDown, CaretRight, XLg} from "react-bootstrap-icons";
import AccessTokenDetailsView from "./access-token-details-view";


export default function ClientCredentialsWorkflow(props)
{
    return <div className={"grant-type-workflow"}>
        <ClientCredentialsGrantDetails
            content={() =>
            {
                return <AccessTokenDetailsView accessTokenDetails={props.accessTokenDetails} />
            }}
            remove={props.onRemove}>
        </ClientCredentialsGrantDetails>
    </div>
}

function ClientCredentialsGrantDetails(props)
{
    const [open, setOpen] = useState(true);

    let variant = "dark";
    return (
        <React.Fragment>
            <Alert className={"collapse-header"}
                   variant={variant}
                   onClick={() =>
                   {
                       setOpen(!open);
                   }}
            >
                {
                    open === true &&
                    <CaretDown />
                }
                {
                    open === false &&
                    <CaretRight />
                }
                Client Credentials Grant
                {
                    props.remove !== undefined &&
                    <XLg onClick={props.remove} className={"remove-collapse"} />
                }
            </Alert>
            <Collapse in={open}>
                <Card className={"workflow-card"}>
                    <Card.Body>
                        {props.content()}
                    </Card.Body>
                </Card>
            </Collapse>
        </React.Fragment>
    );
}