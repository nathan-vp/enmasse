/*
 * Copyright 2018-2019, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

// Code generated by client-gen. DO NOT EDIT.

package v1alpha1

import (
	v1alpha1 "github.com/enmasseproject/enmasse/pkg/apis/iot/v1alpha1"
	scheme "github.com/enmasseproject/enmasse/pkg/client/clientset/versioned/scheme"
	v1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	types "k8s.io/apimachinery/pkg/types"
	watch "k8s.io/apimachinery/pkg/watch"
	rest "k8s.io/client-go/rest"
)

// IoTConfigsGetter has a method to return a IoTConfigInterface.
// A group's client should implement this interface.
type IoTConfigsGetter interface {
	IoTConfigs(namespace string) IoTConfigInterface
}

// IoTConfigInterface has methods to work with IoTConfig resources.
type IoTConfigInterface interface {
	Create(*v1alpha1.IoTConfig) (*v1alpha1.IoTConfig, error)
	Update(*v1alpha1.IoTConfig) (*v1alpha1.IoTConfig, error)
	UpdateStatus(*v1alpha1.IoTConfig) (*v1alpha1.IoTConfig, error)
	Delete(name string, options *v1.DeleteOptions) error
	DeleteCollection(options *v1.DeleteOptions, listOptions v1.ListOptions) error
	Get(name string, options v1.GetOptions) (*v1alpha1.IoTConfig, error)
	List(opts v1.ListOptions) (*v1alpha1.IoTConfigList, error)
	Watch(opts v1.ListOptions) (watch.Interface, error)
	Patch(name string, pt types.PatchType, data []byte, subresources ...string) (result *v1alpha1.IoTConfig, err error)
	IoTConfigExpansion
}

// ioTConfigs implements IoTConfigInterface
type ioTConfigs struct {
	client rest.Interface
	ns     string
}

// newIoTConfigs returns a IoTConfigs
func newIoTConfigs(c *IotV1alpha1Client, namespace string) *ioTConfigs {
	return &ioTConfigs{
		client: c.RESTClient(),
		ns:     namespace,
	}
}

// Get takes name of the ioTConfig, and returns the corresponding ioTConfig object, and an error if there is any.
func (c *ioTConfigs) Get(name string, options v1.GetOptions) (result *v1alpha1.IoTConfig, err error) {
	result = &v1alpha1.IoTConfig{}
	err = c.client.Get().
		Namespace(c.ns).
		Resource("iotconfigs").
		Name(name).
		VersionedParams(&options, scheme.ParameterCodec).
		Do().
		Into(result)
	return
}

// List takes label and field selectors, and returns the list of IoTConfigs that match those selectors.
func (c *ioTConfigs) List(opts v1.ListOptions) (result *v1alpha1.IoTConfigList, err error) {
	result = &v1alpha1.IoTConfigList{}
	err = c.client.Get().
		Namespace(c.ns).
		Resource("iotconfigs").
		VersionedParams(&opts, scheme.ParameterCodec).
		Do().
		Into(result)
	return
}

// Watch returns a watch.Interface that watches the requested ioTConfigs.
func (c *ioTConfigs) Watch(opts v1.ListOptions) (watch.Interface, error) {
	opts.Watch = true
	return c.client.Get().
		Namespace(c.ns).
		Resource("iotconfigs").
		VersionedParams(&opts, scheme.ParameterCodec).
		Watch()
}

// Create takes the representation of a ioTConfig and creates it.  Returns the server's representation of the ioTConfig, and an error, if there is any.
func (c *ioTConfigs) Create(ioTConfig *v1alpha1.IoTConfig) (result *v1alpha1.IoTConfig, err error) {
	result = &v1alpha1.IoTConfig{}
	err = c.client.Post().
		Namespace(c.ns).
		Resource("iotconfigs").
		Body(ioTConfig).
		Do().
		Into(result)
	return
}

// Update takes the representation of a ioTConfig and updates it. Returns the server's representation of the ioTConfig, and an error, if there is any.
func (c *ioTConfigs) Update(ioTConfig *v1alpha1.IoTConfig) (result *v1alpha1.IoTConfig, err error) {
	result = &v1alpha1.IoTConfig{}
	err = c.client.Put().
		Namespace(c.ns).
		Resource("iotconfigs").
		Name(ioTConfig.Name).
		Body(ioTConfig).
		Do().
		Into(result)
	return
}

// UpdateStatus was generated because the type contains a Status member.
// Add a +genclient:noStatus comment above the type to avoid generating UpdateStatus().

func (c *ioTConfigs) UpdateStatus(ioTConfig *v1alpha1.IoTConfig) (result *v1alpha1.IoTConfig, err error) {
	result = &v1alpha1.IoTConfig{}
	err = c.client.Put().
		Namespace(c.ns).
		Resource("iotconfigs").
		Name(ioTConfig.Name).
		SubResource("status").
		Body(ioTConfig).
		Do().
		Into(result)
	return
}

// Delete takes name of the ioTConfig and deletes it. Returns an error if one occurs.
func (c *ioTConfigs) Delete(name string, options *v1.DeleteOptions) error {
	return c.client.Delete().
		Namespace(c.ns).
		Resource("iotconfigs").
		Name(name).
		Body(options).
		Do().
		Error()
}

// DeleteCollection deletes a collection of objects.
func (c *ioTConfigs) DeleteCollection(options *v1.DeleteOptions, listOptions v1.ListOptions) error {
	return c.client.Delete().
		Namespace(c.ns).
		Resource("iotconfigs").
		VersionedParams(&listOptions, scheme.ParameterCodec).
		Body(options).
		Do().
		Error()
}

// Patch applies the patch and returns the patched ioTConfig.
func (c *ioTConfigs) Patch(name string, pt types.PatchType, data []byte, subresources ...string) (result *v1alpha1.IoTConfig, err error) {
	result = &v1alpha1.IoTConfig{}
	err = c.client.Patch(pt).
		Namespace(c.ns).
		Resource("iotconfigs").
		SubResource(subresources...).
		Name(name).
		Body(data).
		Do().
		Into(result)
	return
}
