import { ref } from 'vue'

/**
 * 表单验证
 */
export function useForm<T extends Record<string, any>>(initialValues: T) {
  const form = ref<T>({ ...initialValues } as T)
  const errors = ref<Record<string, string>>({})
  const isSubmitting = ref(false)

  const resetForm = () => {
    form.value = { ...initialValues } as T
    errors.value = {}
  }

  const setErrors = (newErrors: Record<string, string>) => {
    errors.value = newErrors
  }

  const clearErrors = () => {
    errors.value = {}
  }

  const validate = async (rules: Record<keyof T, (value: any) => string | null>[]) => {
    clearErrors()
    let isValid = true

    for (const field in rules) {
      const rule = rules[field as keyof T]
      const error = rule(form.value[field])
      if (error) {
        errors.value[field] = error
        isValid = false
      }
    }

    return isValid
  }

  const handleSubmit = async (callback: (values: T) => Promise<void> | void) => {
    isSubmitting.value = true
    try {
      await callback(form.value)
    } finally {
      isSubmitting.value = false
    }
  }

  return {
    form,
    errors,
    isSubmitting,
    resetForm,
    setErrors,
    clearErrors,
    validate,
    handleSubmit,
  }
}