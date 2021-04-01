import React from "react";
import {Button} from "react-bootstrap";

export default class Modal extends React.Component {

    render() {
        return (
            this.props.show === true &&
            <div role="dialog" aria-modal="true" tabIndex="-1" className={"card-modal"}>
                <div className={"card-modal-content"}>
                    <h5 className={"card-title"}>
                        {this.props.title}
                    </h5>
                    <p>{this.props.message}</p>
                    <Button role="delete" className={"left"} variant="danger" type={"button"}
                            onClick={this.props.onSubmit}>
                        {this.props.submitButtonText}
                    </Button>
                    <Button role="cancel" className={"right"} variant="secondary" type={"button"}
                            onClick={this.props.onCancel}>
                        {this.props.cancelButtonText}
                    </Button>
                </div>
            </div>
        );
    }
}