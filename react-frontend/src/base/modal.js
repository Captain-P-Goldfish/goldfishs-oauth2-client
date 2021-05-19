import React from "react";
import {Button} from "react-bootstrap";

export default class Modal extends React.Component
{

    render()
    {
        return (
            this.props.show === true &&
            <div id={this.props.id} role="dialog" aria-modal="true" tabIndex="-1" className={"card-modal"}>
                <div className={"card-modal-content"}>
                    <h5 id={this.props.id + "-header"} className={"card-title"}>
                        {this.props.title}
                    </h5>
                    <p id={this.props.id + "-text"}>{this.props.message}</p>
                    <Button id={this.props.id + "-button-accept"}
                            role="accept"
                            className={"left"}
                            variant={this.props.variant === undefined ? "info" : this.props.variant}
                            type={"button"}
                            onClick={this.props.onSubmit}>
                        {this.props.submitButtonText}
                    </Button>
                    <Button id={this.props.id + "-button-cancel"}
                            role="cancel"
                            className={"right"}
                            variant="secondary"
                            type={"button"}
                            onClick={this.props.onCancel}>
                        {this.props.cancelButtonText}
                    </Button>
                </div>
            </div>
        );
    }
}