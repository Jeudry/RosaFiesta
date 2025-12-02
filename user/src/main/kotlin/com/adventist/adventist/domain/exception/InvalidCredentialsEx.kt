package com.adventist.adventist.domain.exception

import java.lang.RuntimeException

class InvalidCredentialsEx: RuntimeException("The provided credentials are invalid")