export function toBase64(file)
{
    return new Promise((resolve, reject) =>
    {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () =>
        {
            let encoded = reader.result.toString().replace(/^data:(.*,)?/, '');
            if ((encoded.length % 4) > 0)
            {
                encoded += '='.repeat(4 - (encoded.length % 4));
            }
            resolve(encoded);
        };
        reader.onerror = error => reject(error);
    });
}

export class Optional
{
    constructor(value)
    {
        this.value = value;
    }

    get()
    {
        return this.value;
    }

    isPresent()
    {
        return this.value !== undefined && this.value !== null;
    }

    ifPresent(handler)
    {
        if (this.isPresent())
        {
            handler(this.value);
        }
        return this;
    }

    ifNotPresent(handler)
    {
        if (!this.isPresent())
        {
            handler();
        }
        return this;
    }

    isEmpty()
    {
        return this.value === undefined || this.value === null;
    }

    filter(handler)
    {
        if (this.isPresent() && !handler(this.value))
        {
            this.value = null;
        }
        return this;
    }

    map(handler)
    {
        if (this.isPresent())
        {
            this.value = handler(this.value);
        }
        return this;
    }

    do(handler)
    {
        if (this.isPresent())
        {
            handler(this.value);
        }
        return this;
    }

    orElse(defaultValue)
    {
        if (this.isPresent())
        {
            return this.value;
        }
        else
        {
            return defaultValue;
        }
    }
}
