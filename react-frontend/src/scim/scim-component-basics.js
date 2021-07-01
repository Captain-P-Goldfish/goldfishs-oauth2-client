import {Optional} from "../services/utils";
import * as lodash from "lodash";

export default class ScimComponentBasics
{
    constructor({
                    scimClient,
                    formReference,
                    getOriginalResource,
                    getCurrentResource,
                    setCurrentResource,
                    setState,
                    onCreateSuccess,
                    onUpdateSuccess,
                    onDeleteSuccess
                } = {})
    {
        this.scimClient = scimClient;
        this.formReference = formReference;
        this.getOriginalResource = getOriginalResource;
        this.getCurrentResource = getCurrentResource;
        this.setCurrentResource = setCurrentResource;
        this.setState = setState;
        this.onCreateSuccess = onCreateSuccess;
        this.onUpdateSuccess = onUpdateSuccess;
        this.onDeleteSuccess = onDeleteSuccess;

        this.onSubmit = this.onSubmit.bind(this);
        this.createResource = this.createResource.bind(this);
        this.updateResource = this.updateResource.bind(this);
        this.deleteResource = this.deleteResource.bind(this);
        this.setStateValue = this.setStateValue.bind(this);
        this.resetEditMode = this.resetEditMode.bind(this);
        this.updateInput = this.updateInput.bind(this);
    }

    onSubmit(e)
    {
        e.preventDefault();
        if (new Optional(this.getCurrentResource().id).isPresent())
        {
            this.updateResource();
        }
        else
        {
            this.createResource();
        }
    }

    async createResource()
    {
        let resource = await this.scimClient.getResourceFromFormReference(this.formReference);
        console.log(resource)
        let response = await this.scimClient.createResource(resource);
        this.handleCreateOrUpdateResponse(response, this.onCreateSuccess);
    }

    async updateResource()
    {
        let resource = await this.scimClient.getResourceFromFormReference(this.formReference);
        let response = await this.scimClient.updateResource(resource, this.getOriginalResource().id);
        this.handleCreateOrUpdateResponse(response, this.onUpdateSuccess);
    }

    handleCreateOrUpdateResponse(response, callback)
    {
        if (response.success)
        {
            response.resource.then(resource =>
            {
                this.setCurrentResource(resource);
                this.setState({
                    editMode: false,
                    success: true
                });
                callback(resource);
            })
        }
    }

    deleteResource()
    {
        new Optional(this.getCurrentResource().id).ifNotPresent(id =>
        {
            this.onDeleteSuccess(undefined);
        }).ifPresent(async id =>
        {
            await this.scimClient.deleteResource(id);
            this.onDeleteSuccess(id);
        })
    }

    setStateValue(name, value)
    {
        this.setState({
            [name]: value,
            success: false
        });
    }

    resetEditMode()
    {
        let copiedResource = JSON.parse(JSON.stringify(this.getOriginalResource()))
        this.setCurrentResource(copiedResource);
        this.setState({
            editMode: false,
            success: false
        });
    }

    updateInput(fieldname, value)
    {
        let object = this.getCurrentResource();
        object = lodash.set(object, fieldname, value);
        this.setCurrentResource(object);
        this.setState({
            success: false
        });
    }
} 